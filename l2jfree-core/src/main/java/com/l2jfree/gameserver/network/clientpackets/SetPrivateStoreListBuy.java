/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.network.clientpackets;

import static com.l2jfree.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

import com.l2jfree.Config;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.Shutdown.DisableType;
import com.l2jfree.gameserver.model.TradeList;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.util.Util;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private static final String	_C__91_SETPRIVATESTORELISTBUY	= "[C] 91 SetPrivateStoreListBuy";

	private static final int BATCH_LENGTH = 12; // length of the one item
	private static final int BATCH_LENGTH_FINAL = 40;

	private Item[] _items = null;

	@Override
	protected void readImpl()
	{
		int count = readD();
		if (count < 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * (Config.PACKET_FINAL ? BATCH_LENGTH_FINAL : BATCH_LENGTH) != getByteBuffer().remaining())
		{
			return;
		}

		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			/*_unk1=*/readH();//TODO: analyse this
			/*_unk2=*/readH();//TODO: analyse this
			long cnt = readCompQ();
			long price = readCompQ();

			if (itemId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			if (Config.PACKET_FINAL)
			{
				readC(); // FE
				readD(); // FF 00 00 00
				readD(); // 00 00 00 00
				readB(new byte[7]); // Completely Unknown
			}
			_items[i] = new Item(itemId, cnt, price);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_items == null)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}

		if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			player.sendMessage("Transactions are not allowed during restart/shutdown.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		TradeList tradeList = player.getBuyList();
		tradeList.clear();

		// Check maximum number of allowed slots for pvt shops
		if (_items.length > player.getPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}

		int totalCost = 0;
		for (Item i : _items)
		{
			if (!i.addToTradeList(tradeList))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character "
						+ player.getName() + " of account "
						+ player.getAccountName() + " tried to set price more than "
						+ MAX_ADENA + " adena in Private Store - Buy.",
						Config.DEFAULT_PUNISH);
				return;
			}

			totalCost += i.getCost();
			if (totalCost > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character "
						+ player.getName() + " of account "
						+ player.getAccountName() + " tried to set total price more than "
						+ MAX_ADENA + " adena in Private Store - Buy.",
						Config.DEFAULT_PUNISH);
				return;
			}
		}

		// Check for available funds
		if (totalCost > player.getAdena())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
			return;
		}

		player.sitDown();
		player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}

	private class Item
	{
		private final int _itemId;
		private final long _count;
		private final long _price;

		public Item(int id, long num, long pri)
		{
			_itemId = id;
			_count = num;
			_price = pri;
		}

		public boolean addToTradeList(TradeList list)
		{
			if ((MAX_ADENA / _count) < _price)
				return false;

			list.addItemByItemId(_itemId, _count, _price);
			return true;
		}

		public long getCost()
		{
			return _count * _price;
		}
	}

	@Override
	public String getType()
	{
		return _C__91_SETPRIVATESTORELISTBUY;
	}
}
