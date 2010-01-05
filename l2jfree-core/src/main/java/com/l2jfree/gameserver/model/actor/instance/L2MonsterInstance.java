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

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.knownlist.CharKnownList;
import com.l2jfree.gameserver.model.actor.knownlist.MonsterKnownList;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.util.MinionList;
import com.l2jfree.tools.random.Rnd;

/**
 * This class manages all Monsters.
 * 
 * L2MonsterInstance :<BR><BR>
 * <li>L2MinionInstance</li>
 * <li>L2RaidBossInstance </li>
 * 
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class L2MonsterInstance extends L2Attackable
{
	protected final MinionList		_minionList;

	protected ScheduledFuture<?>	_maintenanceTask				= null;

	private static final int		MONSTER_MAINTENANCE_INTERVAL	= 1000;

	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 * 
	 * @param objectId Identifier of the object to initialized
	 * @param L2NpcTemplate Template to apply to the NPC
	 */
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		if (getTemplate().getMinionData() != null)
			_minionList  = new MinionList(this);
		else
			_minionList = null;

		// [L2J_JP ADD]
		if (getNpcId() == 29002) // Queen Ant Larva is invulnerable.
		{
			setIsInvul(true);
		}
	}
	
	@Override
	protected CharKnownList initKnownList()
	{
		return new MonsterKnownList(this);
	}
	
	@Override
	public final MonsterKnownList getKnownList()
	{
		return (MonsterKnownList)_knownList;
	}

	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2MonsterInstance)
			return false;

		return true;
	}

	/**
	 * Return True if the L2MonsterInstance is Agressive (aggroRange > 0).<BR><BR>
	 */
	@Override
	public boolean isAggressive()
	{
		return getTemplate().getAggroRange() > 0;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		if (getRightHandItem() > 0 && Config.ALT_MONSTER_HAVE_ENCHANTED_WEAPONS)
			setWeaponEnchantLevel(Rnd.get(16));

		if (_minionList != null)
		{
			if (getSpawnedMinions() != null)
			{
				for (L2MinionInstance minion : getSpawnedMinions())
				{
					if (minion == null)
						continue;
					getSpawnedMinions().remove(minion);
					minion.deleteMe();
				}
				_minionList.clearRespawnList();
			}
		}
		startMaintenanceTask();
	}
	
	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}

	/**
	 * Spawn all minions at a regular interval
	 *
	 */
	protected void startMaintenanceTask()
	{
		// maintenance task now used only for minions spawn
		if (_minionList == null)
			return;
		
		_maintenanceTask = ThreadPoolManager.getInstance().schedule(new Runnable()
		{
			public void run()
			{
				_minionList.spawnMinions();
			}
		}, getMaintenanceInterval() + Rnd.get(1000));
	}

	public void callMinions()
	{
		if (hasMinions())
		{
			for (L2MinionInstance minion : _minionList.getSpawnedMinions())
			{
				if (minion == null || minion.isDead() || minion.isInCombat() || minion.isMovementDisabled())
					continue;
				
				// Get actual coords of the minion and check to see if it's too far away from this L2MonsterInstance
				if (!isInsideRadius(minion, 200, false, false))
				{
					// Calculate a new random coord for the minion based on the master's coord
					// but with minimum distance from master = 30
					int minionX = Rnd.nextInt(340);
					int minionY = Rnd.nextInt(340);
					
					if (minionX < 171)
						minionX = getX() + minionX + 30;
					else
						minionX = getX() - minionX + 140;
					
					if (minionY < 171)
						minionY = getY() + minionY + 30;
					else
						minionY = getY() - minionY + 140;
					
					// Move the minion to the new coords
					minion.moveToLocation(minionX, minionY, getZ(), 0);
				}
			}
		}
	}

	public void callMinionsToAssist(L2Character attacker)
	{
		if (hasMinions())
		{
			for (L2MinionInstance minion : _minionList.getSpawnedMinions())
			{
				if (minion == null || minion.isDead())
					continue;
				
				// Trigger the aggro condition of the minion
				if (isRaid() && !isRaidMinion())
					minion.addDamage(attacker, 100, null);
				else
					minion.addDamage(attacker, 1, null);
			}
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!isKillable())
			return false;

		if (!super.doDie(killer))
			return false;

		if (_maintenanceTask != null)
			_maintenanceTask.cancel(true); // doesn't do it?

		if (hasMinions() && isRaid())
			deleteSpawnedMinions();
		return true;
	}

	public List<L2MinionInstance> getSpawnedMinions()
	{
		if (_minionList == null)
			return null;
		return _minionList.getSpawnedMinions();
	}

	public int getTotalSpawnedMinionsInstances()
	{
		if (_minionList == null)
			return 0;
		return _minionList.countSpawnedMinions();
	}

	public int getTotalSpawnedMinionsGroups()
	{
		if (_minionList == null)
			return 0;
		return _minionList.lazyCountSpawnedMinionsGroups();
	}

	public void notifyMinionDied(L2MinionInstance minion)
	{
		_minionList.moveMinionToRespawnList(minion);
	}

	public void notifyMinionSpawned(L2MinionInstance minion)
	{
		_minionList.addSpawnedMinion(minion);
	}

	public boolean hasMinions()
	{
		if (_minionList == null)
			return false;
		return _minionList.hasMinions();
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (!(attacker instanceof L2MonsterInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}

	@Override
	public void deleteMe()
	{
		if (hasMinions())
		{
			if (_maintenanceTask  != null)
				_maintenanceTask .cancel(true);

			deleteSpawnedMinions();
		}
		super.deleteMe();
	}

	public void deleteSpawnedMinions()
	{
		for (L2MinionInstance minion : getSpawnedMinions())
		{
			if (minion == null)
				continue;
			minion.abortAttack();
			minion.abortCast();
			minion.deleteMe();
			getSpawnedMinions().remove(minion);
		}
		_minionList.clearRespawnList();
	}
}
