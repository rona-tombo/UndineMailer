/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.undine;

import java.util.ArrayList;
import java.util.HashMap;

import org.bitbucket.ucchy.undine.sender.MailSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * 添付ボックス管理クラス
 * @author ucchy
 */
public class AttachmentBoxManager {

    protected static final String BOX_INV_META_NAME = "undine_boxinv";

    private Undine parent;

    private HashMap<Player, Inventory> editmodeBoxes;
    private HashMap<Integer, Inventory> attachmentBoxes;
    private HashMap<Player, Integer> indexCache;

    /**
     * コンストラクタ
     * @param parent プラグイン
     */
    public AttachmentBoxManager(Undine parent) {
        this.parent = parent;
        editmodeBoxes = new HashMap<Player, Inventory>();
        attachmentBoxes = new HashMap<Integer, Inventory>();
        indexCache = new HashMap<Player, Integer>();
    }

    /**
     * 指定されたプレイヤーに、そのプレイヤーの編集中ボックスを表示する
     * @param player プレイヤー
     * @param インベントリ名
     */
    protected String displayEditmodeBox(Player player) {

        // 既に、該当プレイヤーの編集中ボックスインベントリがある場合は、そちらを表示する
        if ( editmodeBoxes.containsKey(player) ) {
            player.openInventory(editmodeBoxes.get(player));
            return editmodeBoxes.get(player).getName();
        }

        // 添付ボックスの作成
        int size = parent.getUndineConfig().getAttachBoxSize() * 9;
        String title = Messages.get("EditmodeBoxTitle");

        // インベントリタイトルに32文字以上は設定できないので、必要に応じて削る
        if ( title.length() > 32 ) {
            title = title.substring(0, 32);
        }

        Inventory box = Bukkit.createInventory(player, size, title);

        editmodeBoxes.put(player, box);
        player.openInventory(box);
        return box.getName();
    }

    /**
     * 指定されたプレイヤーの編集中ボックスを取得する
     * @param player プレイヤー
     * @return 編集中ボックス
     */
    protected Inventory getEditmodeBox(Player player) {

        if ( editmodeBoxes.containsKey(player) ) {
            return editmodeBoxes.get(player);
        }
        return null;
    }

    /**
     * 該当プレイヤーの編集中ボックスをクリアする
     * @param player プレイヤー
     */
    protected void clearEditmodeBox(Player player) {

        if ( editmodeBoxes.containsKey(player) ) {
            editmodeBoxes.remove(player);
        }
    }

    /**
     * 指定されたメールの添付ボックスを開いて確認する
     * @param player 確認する人
     * @param mail メール
     * @param インベントリ名
     */
    protected String displayAttachmentBox(Player player, MailData mail) {

        // 既に、該当メールの添付ボックスインベントリがある場合は、そちらを表示する
        if ( attachmentBoxes.containsKey(mail.getIndex()) ) {
            player.openInventory(attachmentBoxes.get(mail.getIndex()));
            return attachmentBoxes.get(mail.getIndex()).getName();
        }

        // 添付ボックスの作成
        int size = (int)((mail.getAttachments().size() - 1) / 9) * 9;
        String title = Messages.get("AttachmentBoxTitle", "%number", mail.getIndex());

        // インベントリタイトルに32文字以上は設定できないので、必要に応じて削る
        if ( title.length() > 32 ) {
            title = title.substring(0, 32);
        }

        Inventory box = Bukkit.createInventory(player, size, title);

        // アイテムを追加
        for ( ItemStack item : mail.getAttachments() ) {
            box.addItem(item);
        }

        attachmentBoxes.put(mail.getIndex(), box);

        // 元のメールの添付ボックスはからにする
        mail.setAttachments(new ArrayList<ItemStack>());

        // 指定されたplayerの画面に添付ボックスを表示する
        player.openInventory(box);

        return box.getName();
    }

    /**
     * 指定されたメールの添付ボックスを開いて確認する
     * @param player 確認する人
     * @param mail メール
     */
    public void displayAttachBox(Player player, MailData mail) {

        String invname;
        if ( mail.isEditmode() ) {
            invname = parent.getBoxManager().displayEditmodeBox(player);
        } else {
            invname = parent.getBoxManager().displayAttachmentBox(player, mail);
        }

        // プレイヤーにメタデータを仕込む
        player.setMetadata(BOX_INV_META_NAME, new FixedMetadataValue(parent, invname));

        // メールのインデクスを記録しておく
        indexCache.put(player, mail.getIndex());
    }

    /**
     * 指定されたプレイヤーが開いていた添付ボックスを、メールと同期する
     * @param player プレイヤー
     */
    protected void syncAttachBox(Player player) {

        if ( !indexCache.containsKey(player) ) return;

        int index = indexCache.get(player);

        MailData mail;
        Inventory inv;
        if ( index == 0 ) {
            mail = parent.getMailManager().getEditmodeMail(MailSender.getMailSender(player));
            inv = editmodeBoxes.get(player);
        } else {
            mail = parent.getMailManager().getMail(index);
            inv = attachmentBoxes.get(index);
        }

        ArrayList<ItemStack> array = new ArrayList<ItemStack>();
        for ( ItemStack item : inv.getContents() ) {
            if ( item != null && item.getType() != Material.AIR ) {
                array.add(item);
            }
        }

        mail.setAttachments(array);
        parent.getMailManager().saveMail(mail);
    }
}

