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
package com.l2jfree.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

public class L2CastleDoormenInstance extends L2DoormenInstance
{
	public L2CastleDoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Chat"))
		{
			showMessageWindow(player);
		}
		else if (command.startsWith("open_doors"))
		{
			if (isOwnerClan(player)
					&& (!getCastle().getSiege().getIsInProgress() || Config.SIEGE_GATE_CONTROL)
					&& (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
			{
				StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
				st.nextToken();

				while (st.hasMoreTokens())
				{
					getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
				}
			}
		}
		else if (command.startsWith("close_doors"))
		{
			if (isOwnerClan(player)
					&& (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
			{
				StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
				st.nextToken();

				while (st.hasMoreTokens())
				{
					getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
				}
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

		if (!isOwnerClan(player))
		{
			html.setFile("data/html/doormen/"+ getNpcId() + "-no.htm");
		}
		else if (getCastle().getSiege().getIsInProgress())
		{
			html.setFile("data/html/doormen/"+ getNpcId() + "-busy.htm");
		}
		else
		{
			html.setFile("data/html/doormen/"+ getNpcId() + ".htm");
		}

		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	private final boolean isOwnerClan(L2PcInstance player)
	{
		if (player.isGM())
			return true;
		if (player.getClan() != null && getCastle() != null)
		{
			if (player.getClanId() == getCastle().getOwnerId())
				return true;
		}
		return false;
	}
}
