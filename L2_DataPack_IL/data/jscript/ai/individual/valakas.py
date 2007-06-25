# L2J_JP CREATE SANDMAN
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.instancemanager import ValakasManager

# Main Quest Code
class valakas(JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    st = player.getQuestState("valakas")
    if not st : return "<html><head><body>I have no tasks for you</body></html>"
    npcId = npc.getNpcId()
    if npcId == 31385 :    # Heart of Volcano
      if st.getInt("ok"):
        if ValakasManager.getInstance().isEnableEnterToLair():
          ValakasManager.getInstance().setValakasSpawnTask()
          ValakasManager.getInstance().addPlayerToLair(st.player)
          st.player.teleToLocation(203940,-111840,66)
          return
        else:
          st.exitQuest(1)
          return "<html><body>Heart of volcano:<br><br>Valakas has already awoke!<br>You are not possible to enter into Lair of Valakas.<br></body></html>"
      else:
        st.exitQuest(1)
        return "Conditions are not right to enter to Lair of Valakas."
    elif npcId == 31540 :    # クライン
      if st.getQuestItemsCount(7267) > 0 :    # 使い捨て浮遊石
        st.takeItems(7267,1)
        st.getPlayer().teleToLocation(183831,-115457,-3296)
        st.set("ok","1")
      else :
        st.exitQuest(1)
        return '<html><head><body>ヴァラカスの監視者 クライン:<br>必要なアイテムを持っていません。</body></html>'
    return

  def onKill (self,npc,player):
    st = player.getQuestState("valakas")
    if not st: return
    ValakasManager.getInstance().setCubeSpawn()
    st.exitQuest(1)

# Quest class and state definition
QUEST       = valakas(-1,"valakas","ai")
CREATED     = State('Start',QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)
# Quest NPC starter initialization
QUEST.addStartNpc(31540)
QUEST.addStartNpc(31385)
QUEST.addTalkId(31540)
QUEST.addTalkId(31385)
QUEST.addKillId(29028)

print "AI: individuals: Valakas...loaded!"
