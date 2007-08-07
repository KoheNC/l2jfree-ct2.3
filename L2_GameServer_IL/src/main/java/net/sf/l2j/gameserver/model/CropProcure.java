/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.model;

/**
 * @author zabbix
 *
 */
public class CropProcure
{
    private int _cropId;
    private int _canBuy;
    private int _rewardType;
    
    public CropProcure(int id, int amount, int type)
    {
        _cropId = id;
        _canBuy = amount;
        _rewardType = type;
    }
    
    public int getReward(){return _rewardType;}
    public int getId(){return _cropId;}
    public int getAmount(){return _canBuy;}
    // For edit the crops data by L2Emu team 26 - 19
    public void setReward(int rewardtype){ _rewardType = rewardtype;}
    public void setId(int id){ _cropId = id; }
    public void setAmount(int Amount){ _canBuy = Amount;}
}
