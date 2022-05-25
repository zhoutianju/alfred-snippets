package priv.zhoutj.tools.alfred.support;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zhoutianju
 * @since 2022/5/25
 */
@Component
public class AlfredSupport {

    // Alfred snippets configuration dir, which sync from macOS by tools like Dropbox
    @Value("${alfred-snippets.alfred-snippets-configuration-dir}")
    public String alfredSnippetsConfigurationDir;

    @Autowired
    private ClipboardSupport clipboardSupport;

    // PREFIX-List<CMD> mapping
    public final LinkedHashMap<String, List<String>> allCmds = new LinkedHashMap<>();
    // flat List<CMD> include all PREFIX
    public final List<String> flatCmds = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            // init allCmds and flatCmds
            List<Path> paths = Files.list(new File(alfredSnippetsConfigurationDir).toPath())
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            for (Path path : paths) {
                String prefix = path.getFileName().toString();
                List<String> cmds = Files.list(path).map(p -> p.getFileName().toString())
                        .filter(s -> s.endsWith(".json"))
                        .collect(Collectors.toList());
                allCmds.put(prefix, cmds);
                cmds.forEach(cmd -> flatCmds.add(prefix + "\\" + cmd));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String findSnippetByPrefixAndCmd(String prefix, String cmd) throws IOException {
        String filepath;
        if (prefix.equals("?")) {
            filepath = flatCmds.stream()
                    .filter(c -> c.matches(cmd.replaceAll("\\\\", "\\\\\\\\") + " \\[.*]\\.json"))
                    .findFirst()
                    .orElse("");
        } else {
            filepath = prefix + "\\" + allCmds.get(prefix).stream()
                    .filter(c -> c.matches(cmd + " \\[.*]\\.json"))
                    .findFirst()
                    .orElse("");
        }
        String absolutePath = alfredSnippetsConfigurationDir + "\\" + filepath;
        String fileContent = new String(Files.readAllBytes(new File(absolutePath).toPath()));
        return (String) ((Map<?, ?>) new Gson().fromJson(fileContent, Map.class).get("alfredsnippet")).get("snippet");
    }

    /**
     * Replace {clipboard} expressions supported by Alfred snippet with local clipboard history
     *
     * @param snippet Alfred snippet
     * @return result after replacement
     * @throws SQLException database operation exception
     */
    public String replaceExp(String snippet) throws SQLException {
        Pattern pattern = Pattern.compile("\\{clipboard(:(\\d+))?}");
        Matcher matcher = pattern.matcher(snippet);
        // match offset-exp mapping
        Map<Integer, String> offsetAndExpMap = new HashMap<>();
        while (matcher.find()) {
            String exp = matcher.group(0);
            // offset of {clipboard} exp set null
            Integer offset = Optional.ofNullable(matcher.group(2)).map(Integer::parseInt).orElse(null);
            offsetAndExpMap.putIfAbsent(offset, exp);
        }
        List<String> clipboardHistoryExcludeInternal = new ArrayList<>();
        // when snippet has {clipboard:x} exp，should exclude internal clipboard history
        if (!offsetAndExpMap.isEmpty() && offsetAndExpMap.keySet().stream().anyMatch(Objects::nonNull)) {
            int maxOffset = offsetAndExpMap.keySet().stream().max(Integer::compareTo).orElse(0);
            int clipboardHistoryOffset = 0;
            for (int i = 0; i <= maxOffset; ) {
                String value = clipboardSupport.clipboardHistoryInDitto(clipboardHistoryOffset);
                // if clipboard history contains, then find next one in Ditto
                if (clipboardSupport.containsInIntervalClipboardHistory(value)) {
                    clipboardHistoryOffset++;
                    continue;
                }
                clipboardHistoryExcludeInternal.add(value);
                i++;
            }
        }
        for (Map.Entry<Integer, String> entry : offsetAndExpMap.entrySet()) {
            Integer offset = entry.getKey();
            String exp = entry.getValue();
            if (offset == null) {
                // offset is null, {clipboard} exp, replace with Ditto newest clipboard history
                snippet = snippet.replace(exp, clipboardSupport.clipboardHistoryInDitto(0));
            } else {
                // offset is not null, {clipboard:x} exp, replace with offset in clipboardHistoryExcludeInternal
                snippet = snippet.replace(exp, clipboardHistoryExcludeInternal.get(offset));
            }
        }
        return snippet;
    }
}
