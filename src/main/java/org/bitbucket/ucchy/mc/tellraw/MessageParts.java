/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.mc.tellraw;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.ChatColor;

/**
 * メッセージパーツ
 * @author ucchy
 */
public class MessageParts {

    private String text;
    private ChatColor color;
    private ClickEventType ctype;
    private String cvalue;
    private HashSet<String> flags;

    public MessageParts(String text) {
        this.text = text;
        this.flags = new HashSet<String>();
    }

    public MessageParts(String text, ChatColor color) {
        this(text);
        setColor(color);
    }

    public MessageParts(String text, ChatColor color, ChatColor color2) {
        this(text);
        setColor(color);
        setColor(color2);
    }

    public void setColor(ChatColor color) {
        if ( color.isColor() ) {
            this.color = color;
        } else if ( color == ChatColor.BOLD ) {
            flags.add("bold");
        } else if ( color == ChatColor.ITALIC ) {
            flags.add("italic");
        } else if ( color == ChatColor.UNDERLINE ) {
            flags.add("underlined");
        } else if ( color == ChatColor.STRIKETHROUGH ) {
            flags.add("strikethrough");
        } else if ( color == ChatColor.MAGIC ) {
            flags.add("obfuscated");
        }
    }

    public void setClickEvent(ClickEventType type, String value) {
        ctype = type;
        cvalue = value;
    }

    public String toString() {

        ArrayList<String> items = new ArrayList<String>();
        items.add("\"text\":\"" + text + "\"");
        if ( color != null ) {
            items.add("\"color\":\"" + color.name().toLowerCase() + "\"");
        }
        for ( String flag : flags ) {
            items.add("\"" + flag + "\":\"true\"");
        }
        if ( ctype != null ) {
            items.add("\"clickEvent\":"
                    + "{\"action\":\"" + ctype.toString() + "\","
                            + "\"value\":\"" + cvalue + "\"}");
        }

        return "{" + join(items) + "}";
    }

    private static String join(ArrayList<String> arr) {
        StringBuffer buffer = new StringBuffer();
        for ( String s : arr ) {
            if ( buffer.length() > 0 ) {
                buffer.append(",");
            }
            buffer.append(s);
        }
        return buffer.toString();
    }
}
