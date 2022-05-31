package priv.zhoutj.tools.alfred.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import priv.zhoutj.tools.alfred.completer.CmdCompleter;
import priv.zhoutj.tools.alfred.completer.PrefixCompleter;
import priv.zhoutj.tools.alfred.support.AlfredSupport;
import priv.zhoutj.tools.alfred.support.ClipboardSupport;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Spring Shell Command
 */
@Slf4j
@ShellComponent
@ShellCommandGroup("alfred commands")
public class Commands {

    @Autowired
    private AlfredSupport alfredSupport;
    @Autowired
    private ClipboardSupport clipboardSupport;

    @ShellMethod(value = "snippet")
    public void snippet(@ShellOption(valueProvider = PrefixCompleter.class) String prefix,
                        @ShellOption(valueProvider = CmdCompleter.class) String cmd) throws IOException {
        String snippet = alfredSupport.findSnippetByPrefixAndCmd(prefix, cmd);
        try {
            String value = alfredSupport.replaceExp(snippet);
            clipboardSupport.putClipboard(value);
            System.out.printf("success to put into clipboard, value: \"%s\".\n", value);
        } catch (Exception e) {
            log.error("error", e);
            System.out.println(snippet);
        }
    }
}
