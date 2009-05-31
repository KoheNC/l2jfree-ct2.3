import sys
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.gameserver.network.serverpackets import NpcSay
from com.l2jfree.tools.random import Rnd

# ol_mahum_general
class ol_mahum_general(JQuest) :

    # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self, id, name, descr) :
        self.ol_mahum_general = 20438
        self.FirstAttacked = False
        # finally, don't forget to call the parent constructor to prepare the event triggering
        # mechanisms etc.
        JQuest.__init__(self,id,name,descr)

    def onAttack (self, npc, player, damage, isPet, skill) :
        objId = npc.getObjectId()
        if self.FirstAttacked :
           if Rnd.get(100) : return
           npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "We shall see about that!"))
        else :
           self.FirstAttacked = True
           npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "I will definitely repay this humiliation!"))
        return 

    def onKill (self, npc, player, isPet) :
        npcId = npc.getNpcId()
        if npcId == self.ol_mahum_general :
            objId = npc.getObjectId()
            self.FirstAttacked = False
        elif self.FirstAttacked :
            self.addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), True, 0)
        return 

QUEST = ol_mahum_general(-1, "ol_mahum_general", "ai")

QUEST.addKillId(QUEST.ol_mahum_general)

QUEST.addAttackId(QUEST.ol_mahum_general)