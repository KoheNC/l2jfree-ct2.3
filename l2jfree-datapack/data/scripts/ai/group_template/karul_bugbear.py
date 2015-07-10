import sys
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.gameserver.network.packets.server import NpcSay
from com.l2jfree.tools.random import Rnd

# karul_bugbear
class karul_bugbear(JQuest) :

    # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self, id, name, descr) :
        self.karul_bugbear = 20600
        self.FirstAttacked = False
        # finally, don't forget to call the parent constructor to prepare the event triggering
        # mechanisms etc.
        JQuest.__init__(self, id, name, descr)

    def onAttack (self, npc, player, damage, isPet, skill) :
        objId = npc.getObjectId()
        if self.FirstAttacked :
           if Rnd.get(4) : return
           npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "Your rear is practically unguarded!"))
        else :
           self.FirstAttacked = True
           if Rnd.get(4) : return
           npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(),"Watch your back!"))
        return 

    def onKill (self, npc, player, isPet) :
        npcId = npc.getNpcId()
        if npcId == self.karul_bugbear :
            objId = npc.getObjectId()
            self.FirstAttacked = False
        elif self.FirstAttacked :
            self.addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), True, 0)
        return 

QUEST = karul_bugbear(-1, "karul_bugbear", "ai")

QUEST.addKillId(QUEST.karul_bugbear)

QUEST.addAttackId(QUEST.karul_bugbear)