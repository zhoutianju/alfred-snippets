package priv.zhoutj.tools.alfred.completer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.stereotype.Component;
import priv.zhoutj.tools.alfred.support.AlfredSupport;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CMD completer
 */
@Component
public class CmdCompleter extends ValueProviderSupport {

    @Autowired
    private AlfredSupport alfredSupport;

    @Override
    public List<CompletionProposal> complete(MethodParameter methodParameter,
                                             CompletionContext completionContext,
                                             String[] strings) {
        String prefix = completionContext.getWords().stream()
                .filter(w -> !w.startsWith("--")).findFirst().orElse("");
        String input = completionContext.currentWord();
        // if prefix is "?", find in flatCmds
        if (prefix.equals("?")) {
            return alfredSupport.flatCmds.stream()
                    .filter(c -> c.contains(input)).map(s -> s.replaceAll(" \\[.*]\\.json", ""))
                    .map(CompletionProposal::new)
                    .collect(Collectors.toList());
        }
        if (!alfredSupport.allCmds.containsKey(prefix)) {
            return Collections.emptyList();
        }
        List<String> cmds = alfredSupport.allCmds.get(prefix);
        return cmds.stream().filter(c -> c.contains(input))
                .map(s -> s.replaceAll(" \\[.*]\\.json", ""))
                .map(CompletionProposal::new)
                .collect(Collectors.toList());
    }
}
