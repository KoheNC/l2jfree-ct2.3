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
package com.l2jfree.gameserver.handler.usercommandhandlers;

import com.l2jfree.gameserver.handler.IUserCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.olympiad.Olympiad;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.packets.server.SystemMessage;

/**
 * Support for /olympiadstat command Added by kamy
 */
public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 109 };
	
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IUserCommandHandler#useUserCommand(int, com.l2jfree.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;
		
		if (!activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMessageId.NOBLESSE_ONLY);
			return false;
		}
		
		SystemMessage sm =
				new SystemMessage(
						SystemMessageId.YOUR_CURRENT_OLYMPIAD_RECORD_S1_MATCHES_S2_WINS_S3_DEFEATS_EARNED_S4_POINTS);
		sm.addNumber(Olympiad.getInstance().getCompetitionDone(activeChar.getObjectId()));
		sm.addNumber(Olympiad.getInstance().getCompetitionWon(activeChar.getObjectId()));
		sm.addNumber(Olympiad.getInstance().getCompetitionLost(activeChar.getObjectId()));
		sm.addNumber(Olympiad.getInstance().getNoblePoints(activeChar.getObjectId()));
		activeChar.sendPacket(sm);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
