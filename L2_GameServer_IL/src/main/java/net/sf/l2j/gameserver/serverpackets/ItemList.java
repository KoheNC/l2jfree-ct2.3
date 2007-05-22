/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 *
 * sample
 *  
 * 27 
 * 00 00 
 * 01 00        // item count
 * 
 * 04 00        // itemType1  0-weapon/ring/earring/necklace  1-armor/shield  4-item/questitem/adena
 * c6 37 50 40  // objectId 
 * cd 09 00 00  // itemId
 * 05 00 00 00  // count
 * 05 00        // itemType2  0-weapon  1-shield/armor  2-ring/earring/necklace  3-questitem  4-adena  5-item
 * 00 00        // always 0 ??
 * 00 00        // equipped 1-yes
 * 00 00        // slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
 * 00 00        // always 0 ??
 * 00 00        // always 0 ??
 *  
 
 * format   h (h dddhhhh hh)    revision 377
 * format   h (h dddhhhd hh)    revision 415
 * format   h (h dddhhhd hhhhd)  729
 * 
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
public class ItemList extends L2GameServerPacket
{
    private final static Log _log = LogFactory.getLog(ItemList.class.getName());
	private static final String _S__27_ITEMLIST = "[S] 1b ItemList";
	private L2ItemInstance[] _items;
	private boolean _showWindow;

	public ItemList(L2PcInstance cha, boolean showWindow)
	{
		_items = cha.getInventory().getItems();
		_showWindow = showWindow;
		if (_log.isDebugEnabled())
		{
			this.showDebug();
		}
	}	

	public ItemList(L2ItemInstance[] items, boolean showWindow)
	{
		_items = items;
		_showWindow = showWindow;
		if (_log.isDebugEnabled())
		{
			this.showDebug();
		}
	}	
	
	private void showDebug()
	{
		for (L2ItemInstance temp : _items)
		{
			_log.info("item:" + temp.getItem().getName() +
					" type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
		}
	}
	
	protected final void writeImpl()
	{
		writeC(0x1b);
		writeH(_showWindow ? 0x01 : 0x00);
		
		int count = _items.length; 
		writeH(count);

		for (L2ItemInstance temp : _items)
		{
			if (temp == null || temp.getItem() == null)
				continue;
			
			writeH(temp.getItem().getType1()); // item type1
			
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());	// item type2
			writeH(temp.getCustomType1());	// item type3
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());

			writeH(temp.getEnchantLevel());	// enchant level
			//race tickets
			writeH(temp.getCustomType2());	// item type3
			
			if (temp.isAugmented())
				writeD(temp.getAugmentation().getAugmentationId());
			else
				writeD(0x00);
			
			writeD(-1); // C6
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__27_ITEMLIST;
	}
}
