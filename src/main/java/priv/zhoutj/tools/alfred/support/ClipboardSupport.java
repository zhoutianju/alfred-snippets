package priv.zhoutj.tools.alfred.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhoutianju
 * @since 2022/5/25
 */
@Component
public class ClipboardSupport {

    static {
        // solve java.awt.HeadlessException when using java.awt.datatransfer.Clipboard
        System.setProperty("java.awt.headless", "false");
    }

    // Ditto Sqlite DB filepath
    @Value("${alfred-snippets.ditto-db-filepath}")
    private String dittoDbFilepath;

    // interval clipboard history, for support exclude interval clipboard history when {clipboard:x} exp in Alfred
    private final Set<String> internalClipboardHistory = new HashSet<>();

    /**
     * put into local Clipboard
     *
     * @param string value
     */
    public void putClipboard(String string) {
        // Ditto save clipboard in DB end with a "\u0000"
        internalClipboardHistory.add(string + "\u0000");
        StringSelection selection = new StringSelection(string);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public boolean containsInIntervalClipboardHistory(String string) {
        return internalClipboardHistory.contains(string);
    }

    /**
     * Get clipboard history from Ditto sqlite DB (only returns CF_UNICODETEXT type)
     *
     * @param offset offset
     * @return clipboard history value
     * @throws SQLException database operation exception
     */
    public String clipboardHistoryInDitto(int offset) throws SQLException {
        String value;
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dittoDbFilepath);
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(String.format("select d.ooData from Main m " +
                "join Data d on m.lID = d.lParentID " +
                "where d.strClipBoardFormat = 'CF_UNICODETEXT' " +
                "order by m.clipOrder " +
                "desc limit %s, 1;", offset));
        rs.next();
        value = rs.getString("ooData");
        connection.close();
        return value;
    }
}
