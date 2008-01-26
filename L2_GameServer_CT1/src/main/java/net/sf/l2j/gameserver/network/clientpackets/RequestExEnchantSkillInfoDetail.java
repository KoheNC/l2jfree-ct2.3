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
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Format (ch) ddd
 * c: (id) 0xD0
 * h: (subid) 0x31
 * d: skill id
 * d: skill lvl ?
 * d: ?
 * @author -Wooden-
 *
 */
public final class RequestExEnchantSkillInfoDetail extends L2GameClientPacket
{
    private final static Log _log = LogFactory.getLog(RequestExEnchantSkillInfoDetail.class.getName());
    
    private static final int TYPE_NORMAL_ENCHANT = 0;
    private static final int TYPE_SAFE_ENCHANT = 1;
    private static final int TYPE_UNTRAIN_ENCHANT = 2;
    private static final int TYPE_CHANGE_ENCHANT = 3;
    
    private int _type;
    private int _skillId;
    private int _skillLvl;
	
	@Override
    protected void readImpl()
	{
        _type = readD();
		_skillId = readD();
		_skillLvl = readD();
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
        
        if (activeChar == null) 
            return;
        
        int bookId = 0;
        int reqCount = 0;
        // require book for first level
        int enchantLevel = _skillLvl%100;
        // if going to first level OR going to Original level(untraining) OR changing route then require book
        if ((_skillLvl > 100 && enchantLevel == 1) || (_skillLvl < 100) || _type == TYPE_CHANGE_ENCHANT)
        {
            switch (_type)
            {
                case TYPE_NORMAL_ENCHANT:
                    bookId = SkillTreeTable.NORMAL_ENCHANT_BOOK;
                    reqCount = 1;
                    break;
                case TYPE_SAFE_ENCHANT:
                    bookId = SkillTreeTable.SAFE_ENCHANT_BOOK;
                    reqCount = 1;
                    break;
                case TYPE_UNTRAIN_ENCHANT:
                    bookId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;
                    reqCount = 1;
                    break;
                case TYPE_CHANGE_ENCHANT:
                    bookId = SkillTreeTable.CHANGE_ENCHANT_BOOK;
                    reqCount = 1;
                    break;
                default:
                    _log.fatal("Unknown skill enchant type: "+_type);
                return;
            }
        }
        
        // send skill enchantment detail
        ExEnchantSkillInfoDetail esd = new ExEnchantSkillInfoDetail(bookId, reqCount);
        activeChar.sendPacket(esd);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[C] D0:31 RequestExEnchantSkillInfo";
	}
	
}
