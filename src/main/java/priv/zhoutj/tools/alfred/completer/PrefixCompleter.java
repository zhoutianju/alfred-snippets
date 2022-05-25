package priv.zhoutj.tools.alfred.completer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.stereotype.Component;
import priv.zhoutj.tools.alfred.support.AlfredSupport;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PREFIX completer
 */
@Component
public class PrefixCompleter extends ValueProviderSupport {

    @Autowired
    private AlfredSupport alfredSupport;

    @Override
    public List<CompletionProposal> complete(MethodParameter methodParameter,
                                             CompletionContext completionContext, String[] strings) {
        String input = completionContext.currentWord();
        return alfredSupport.allCmds.keySet().stream().filter(p -> p.contains(input))
                .map(CompletionProposal::new)
                .collect(Collectors.toList());
    }
}
