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
package com.l2jfree.gameserver.handler.itemhandlers;

import com.l2jfree.Config;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ExChooseInventoryAttributeItem;

public class EnchantAttr implements IItemHandler
{
	private static final int	ITEM_IDS[]	=
											{
			9546,
			9547,
			9548,
			9549,
			9550,
			9551,
			9552,
			9553,
			9554,
			9555,
			9556,
			9557,
			9558,
			9559,
			9560,
			9561,
			9562,
			9563,
			9564,
			9565,
			9566,
			9567,
			9568,
			9569							};

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		if (activeChar.isCastingNow())
			return;

		// Restrict enchant during restart/shutdown (because of an existing exploit)
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_ENCHANT && Shutdown.getCounterInstance() != null
				&& Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			activeChar.sendMessage("Enchanting items is not allowed during restart/shutdown.");
			return;
		}

		activeChar.sendPacket(SystemMessageId.SELECT_ITEM_TO_ADD_ELEMENTAL_POWER);
		activeChar.setActiveEnchantAttrItem(item);
		activeChar.sendPacket(new ExChooseInventoryAttributeItem(item.getItemId()));
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
