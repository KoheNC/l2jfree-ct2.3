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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * @author Kerberos
 */

public class L2TownPetInstance extends L2NpcInstance
{    
    int randomX, randomY, spawnX, spawnY;

    public L2TownPetInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);

        ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RandomWalkTask(), 2000, 4000);
    }

    public void onAction(L2PcInstance player)
    {
        if (!canTarget(player)) return;
        
        if (this != player.getTarget())
        {
            // Set the target of the L2PcInstance player
            player.setTarget(this);

            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            // The color to display in the select window is White
            MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
            player.sendPacket(my);

            // Send a Server->Client packet ValidateLocation to correct the L2ArtefactInstance position and heading on the client
            player.sendPacket(new ValidateLocation(this));
        }
        else
        {
            // Calculate the distance between the L2PcInstance and the L2NpcInstance
            if (!canInteract(player))
            {
                // Notify the L2PcInstance AI with AI_INTENTION_INTERACT
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
            }
        }
        // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
        player.sendPacket(new ActionFailed());
    }

    public void onSpawn()
    {
        super.onSpawn();
        spawnX = getX();
        spawnY = getY();
    }
    public class RandomWalkTask implements Runnable
    {
        public void run()
        {
            randomX = spawnX + Rnd.get(0,2)*40;
            randomY = spawnY + Rnd.get(0,2)*40;

            if ((randomX != getX()) && (randomY != getY()))
                getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(randomX,randomY,getZ(),0));
        }
    }
}