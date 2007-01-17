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
package net.sf.l2j.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.StringTokenizer;

import javolution.lang.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.instancemanager.ArenaManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.geodata.GeoDataRequester;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Condition;
import net.sf.l2j.gameserver.skills.EffectCharge;
import net.sf.l2j.gameserver.skills.EffectTemplate;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.skills.FuncTemplate;
import net.sf.l2j.gameserver.skills.L2SkillCharge;
import net.sf.l2j.gameserver.skills.L2SkillChargeDmg;
import net.sf.l2j.gameserver.skills.L2SkillCreateItem;
import net.sf.l2j.gameserver.skills.L2SkillDefault;
import net.sf.l2j.gameserver.skills.L2SkillDrain;
import net.sf.l2j.gameserver.skills.L2SkillSeed;
import net.sf.l2j.gameserver.skills.L2SkillSummon;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * This class...
 * 
 * @version $Revision: 1.3.2.8.2.22 $ $Date: 2005/04/06 16:13:42 $
 */
public abstract class L2Skill
{
    protected static Log _log = LogFactory.getLog(L2Skill.class.getName());
    
    public static final int SKILL_CUBIC_MASTERY = 143;
    public static final int SKILL_LUCKY = 194;
    public static final int SKILL_CREATE_COMMON = 1320; 
    public static final int SKILL_CREATE_DWARVEN = 172;
    public static final int SKILL_CRYSTALLIZE = 248;
    
    public static final int SKILL_FAKE_INT = 9001;
    public static final int SKILL_FAKE_WIT = 9002;
    public static final int SKILL_FAKE_MEN = 9003;
    public static final int SKILL_FAKE_CON = 9004;
    public static final int SKILL_FAKE_DEX = 9005;
    public static final int SKILL_FAKE_STR = 9006;
    
    public static enum SkillOpType { OP_PASSIVE, OP_ACTIVE, OP_TOGGLE }
    
    /** Target types of skills : SELF, PARTY, CLAN, PET... */
    public static enum SkillTargetType 
    {
        TARGET_NONE,
        TARGET_SELF,
        TARGET_ONE,
        TARGET_PARTY,
        TARGET_ALLY,
        TARGET_CLAN,
        TARGET_PET,
        TARGET_AREA,
        TARGET_AURA,
        TARGET_AURA_UNDEAD,
        TARGET_CORPSE,
        TARGET_UNDEAD,
        TARGET_AREA_UNDEAD,
        TARGET_MULTIFACE,
        TARGET_CORPSE_ALLY,
        TARGET_CORPSE_CLAN,
        TARGET_CORPSE_PLAYER,
        TARGET_CORPSE_PET,
        TARGET_ITEM,
        TARGET_AREA_CORPSE_MOB,
        TARGET_CORPSE_MOB,
        TARGET_UNLOCKABLE,
        TARGET_HOLY,
        TARGET_PARTY_MEMBER,
        TARGET_ENEMY_SUMMON,        
        TARGET_OWNER_PET,
        TARGET_ENEMY_ALLY,
        TARGET_ENEMY_PET,
        TARGET_GATE,
        TARGET_MOB,
        TARGET_AREA_MOB
        //TARGET_BOSS
        }
    
    
    public static enum SkillType 
    {
        PDAM,
        MDAM,
        DOT,
        BLEED,
        POISON,
        HEAL,
        BALANCE_HEAL,
        HOT,
        COMBATPOINTHEAL,
        CPHOT,
        MANAHEAL,
        MANAHEAL_PERCENT,
        MANARECHARGE, 
        MPHOT,
        AGGDAMAGE,
        BUFF,
        DEBUFF,
        STUN,
        ROOT,
        RESURRECT,
        PASSIVE,
        CONT,
        CONFUSION,
        UNLOCK,
        CHARGE                (L2SkillCharge.class),
        FEAR,
        MHOT,
        DRAIN                 (L2SkillDrain.class),
        NEGATE,               
        CANCEL,             
        SLEEP,
        AGGREDUCE,
        AGGREMOVE,
        AGGREDUCE_CHAR,
        CHARGEDAM             (L2SkillChargeDmg.class),
        CONFUSE_MOB_ONLY,
        DEATHLINK,
        DETECT_WEAKNESS,
        ENCHANT_ARMOR,
        ENCHANT_WEAPON,
        FEED_PET,
        HEAL_PERCENT,
        LUCK,
        MANADAM,
        MDOT,
        MUTE,
        RECALL,
        REFLECT,
        SOULSHOT,
        SPIRITSHOT,
        SPOIL,
        SWEEP,
        SUMMON                (L2SkillSummon.class),
        WEAKNESS,
        DEATHLINK_PET,
        MANA_BY_LEVEL,
        FAKE_DEATH,
        UNBLEED,
        UNPOISON,
        SIEGEFLAG,
        TAKECASTLE,
        UNDEAD_DEFENSE,
        SEED                (L2SkillSeed.class),
        PARALYZE,
        DRAIN_SOUL,
        COMMON_CRAFT, 
        DWARVEN_CRAFT,
        WEAPON_SA,
        FISHING,
        PUMPING,
        REELING,
        CANCEL_TARGET,
        CHARGE_SELF,
        CRITS,
        CREATE_ITEM      (L2SkillCreateItem.class),
        AGGDEBUFF,
        COMBATPOINTPERHEAL,
        SUMMONCP,
        SUMMON_TREASURE_KEY,
        SUMMON_CURSED_BONES,
        ERASE,
        MAGE_BANE, 
        WARRIOR_BANE,
        STRSIEGEASSAULT, 
        BLESSNOBLESSE, 
        LUCKNOBLESSE    (L2SkillCreateItem.class),
        RAID_DESCRIPTION,
        UNSUMMON_ENEMY_PET,
        BETRAY,
        SERVER_SIDE, //TODO: IMPLEMENT
        // unimplemented
        NOTDONE;
        
        private final Class<? extends L2Skill> _class;
        
        public L2Skill makeSkill(StatsSet set)
        {
            try {
                Constructor<? extends L2Skill> c =
                    _class.getConstructor(StatsSet.class);
                
                return c.newInstance(set);
            } 
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
       private SkillType() 
        {
           _class = L2SkillDefault.class;
       }
        
       private SkillType(Class<? extends L2Skill> classType) 
        {
           _class = classType;
        }
    }
    
    //elements
    public final static int ELEMENT_WIND = 1;
    public final static int ELEMENT_FIRE = 2;
    public final static int ELEMENT_WATER = 3;
    public final static int ELEMENT_EARTH = 4;
    public final static int ELEMENT_HOLY = 5; 
    public final static int ELEMENT_DARK = 6; 
    public final static int ELEMENT_UNHOLY = 5;
    public final static int ELEMENT_SACRED = 6;
    
    //save vs 
    public final static int SAVEVS_INT = 1;
    public final static int SAVEVS_WIT = 2;
    public final static int SAVEVS_MEN = 3;
    public final static int SAVEVS_CON = 4;
    public final static int SAVEVS_DEX = 5;
    public final static int SAVEVS_STR = 6;
    
    //stat effected
    public final static int STAT_PATK = 301;           // pAtk 
    public final static int STAT_PDEF = 302;           // pDef
    public final static int STAT_MATK = 303;           // mAtk
    public final static int STAT_MDEF = 304;           // mDef
    public final static int STAT_MAXHP = 305;          // maxHp
    public final static int STAT_MAXMP = 306;          // maxMp
    public final static int STAT_CURHP = 307;
    public final static int STAT_CURMP = 308;
    public final static int STAT_HPREGEN = 309;        // regHp
    public final static int STAT_MPREGEN = 310;        // regMp
    public final static int STAT_CASTINGSPEED = 311;   // sCast
    public final static int STAT_ATKSPD = 312;         // sAtk
    public final static int STAT_CRITDAM = 313;        // critDmg
    public final static int STAT_CRITRATE = 314;       // critRate
    public final static int STAT_FIRERES = 315;        // fireRes
    public final static int STAT_WINDRES = 316;        // windRes
    public final static int STAT_WATERRES = 317;       // waterRes
    public final static int STAT_EARTHRES = 318;       // earthRes
    public final static int STAT_HOLYRES = 336;        // holyRes 
    public final static int STAT_DARKRES = 337;        // darkRes     
    public final static int STAT_ROOTRES = 319;        // rootRes
    public final static int STAT_SLEEPRES = 320;       // sleepRes
    public final static int STAT_CONFUSIONRES = 321;   // confusRes
    public final static int STAT_BREATH = 322;         // breath
    public final static int STAT_AGGRESSION = 323;     // aggr
    public final static int STAT_BLEED = 324;          // bleed
    public final static int STAT_POISON = 325;         // poison
    public final static int STAT_STUN = 326;           // stun
    public final static int STAT_ROOT = 327;           // root
    public final static int STAT_MOVEMENT = 328;       // move
    public final static int STAT_EVASION = 329;        // evas
    public final static int STAT_ACCURACY = 330;       // accu
    public final static int STAT_COMBAT_STRENGTH = 331;
    public final static int STAT_COMBAT_WEAKNESS = 332;
    public final static int STAT_ATTACK_RANGE = 333;   // rAtk
    public final static int STAT_NOAGG = 334;          // noagg
    public final static int STAT_SHIELDDEF = 335;      // sDef
    public final static int STAT_MP_CONSUME_RATE = 336;      // Rate of mp consume per skill use
    public final static int STAT_HP_CONSUME_RATE = 337;      // Rate of hp consume per skill use
    
    //COMBAT DAMAGE MODIFIER SKILLS...DETECT WEAKNESS AND WEAKNESS/STRENGTH
    public final static int COMBAT_MOD_ANIMAL = 200;
    public final static int COMBAT_MOD_BEAST    = 201;
    public final static int COMBAT_MOD_BUG  = 202;
    public final static int COMBAT_MOD_DRAGON = 203;
    public final static int COMBAT_MOD_MONSTER= 204;
    public final static int COMBAT_MOD_PLANT    = 205;
    public final static int COMBAT_MOD_HOLY = 206;
    public final static int COMBAT_MOD_UNHOLY = 207;
    public final static int COMBAT_MOD_BOW  = 208;
    public final static int COMBAT_MOD_BLUNT    = 209;
    public final static int COMBAT_MOD_DAGGER = 210;
    public final static int COMBAT_MOD_FIST     = 211;
    public final static int COMBAT_MOD_DUAL     = 212;
    public final static int COMBAT_MOD_SWORD    = 213;
    public final static int COMBAT_MOD_POISON = 214;
    public final static int COMBAT_MOD_BLEED    = 215;
    public final static int COMBAT_MOD_FIRE = 216;
    public final static int COMBAT_MOD_WATER    = 217;
    public final static int COMBAT_MOD_EARTH    = 218;
    public final static int COMBAT_MOD_WIND = 219;
    public final static int COMBAT_MOD_ROOT = 220;
    public final static int COMBAT_MOD_STUN = 221;
    public final static int COMBAT_MOD_CONFUSION = 222;
    public final static int COMBAT_MOD_DARK = 223;
    
    //conditional values
    public final static int COND_RUNNING =   0x0001;
    public final static int COND_WALKING =   0x0002;
    public final static int COND_SIT     =   0x0004;
    public final static int COND_BEHIND  =   0x0008;
    public final static int COND_CRIT    =   0x0010;
    public final static int COND_LOWHP   =   0x0020;
    public final static int COND_ROBES   =   0x0040;
    public final static int COND_CHARGES =   0x0080;
    public final static int COND_SHIELD  =   0x0100;
    public final static int COND_GRADEA  = 0x010000;
    public final static int COND_GRADEB  = 0x020000;
    public final static int COND_GRADEC  = 0x040000;
    public final static int COND_GRADED  = 0x080000;
    public final static int COND_GRADES  = 0x100000;
    
    
    private static final Func[] _emptyFunctionSet = new Func[0];
    private static final L2Effect[] _emptyEffectSet = new L2Effect[0];
    
    // these two build the primary key
    private final int _id;
    private final int _level;
    
    /** Instant Kill Rate (iRate) **/
    private final int _iRate;
    private final boolean _iKill;
    
    /** Identifier for a skill that client can't display */
    private int _displayId;

    // not needed, just for easier debug
    private final String _name;
    private final SkillOpType _operateType;
    private final boolean _magic;
    private final int _mpConsume;
    private final int _mpInitialConsume;
    private final int _hpConsume;
    private final int _itemConsume;
    private final int _itemConsumeId;
    // item consume count over time
    private final int _itemConsumeOT;
    // item consume id over time
    private final int _itemConsumeIdOT;
    // item consume time in milliseconds 
    private final int _itemConsumeTime;
    private final int _castRange;
    private final int _effectRange;
    
    // all times in milliseconds 
    private final int _skillTime;
    private final int _skillInterruptTime;
    private final int _hitTime;
    private final int _reuseDelay;
    private final int _buffDuration;
   
    /** Target type of the skill : SELF, PARTY, CLAN, PET... */
    private final SkillTargetType _targetType;
    private final double _power;
    private final int _levelDepend;
    
    // Effecting area of the skill, in radius.
    // The radius center varies according to the _targetType:
    // "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
    private final int _skillRadius;
    
    private final SkillType _skillType;
    private final SkillType _effectType;
    private final int _effectPower;
    private final int _effectLvl;
    private final int _skill_landing_percent;

    private final boolean _ispotion;
    private final int _element;
    private final int _savevs;
    private final boolean _isSuicideAttack;
    private final int _activateRate;
    private final int _levelModifier;
    private final int _magicLevel;
    private final String[] _negateStats;
    private final float _negatePower;
    
    private final Stats _stat;
    
    private final int _condition;
    private final int _conditionValue;
    private final boolean _overhit;
    private final boolean _critical;
    private final boolean _ignoreShld;
    private final int _weaponsAllowed;
    private final int _armorsAllowed;
    
    private final int _addCrossLearn; // -1 disable, otherwice SP price for others classes, default 1000
    private final float _mulCrossLearn; // multiplay for others classes, default 2
    private final float _mulCrossLearnRace; // multiplay for others races, default 2
    private final float _mulCrossLearnProf; // multiplay for fighter/mage missmatch, default 3
    private final List<ClassId> _canLearn; // which classes can learn
    private final List<Integer> _teachers; // which NPC teaches
    private final boolean _isOffensive;
    private final int _num_charges;

    private final int _lethalEffect1;     // percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only for PDAM skills)
    private final int _lethalEffect2;     // percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only for PDAM skills)
    private final boolean _directHpDmg;  // If true then dmg is being make directly 
    private final boolean _isDance;      // If true then casting more dances will cost more MP
    private final int _nextDanceCost;

    private final float _successRate; 
    private final int _minPledgeClass;
    
    protected Condition _preCondition;
    protected Condition _itemPreCondition;
    protected FuncTemplate[] _funcTemplates;
    protected EffectTemplate[] _effectTemplates;
    protected EffectTemplate[] _effectTemplatesSelf;
    
    
    protected L2Skill(StatsSet set)
    {
        _id           = set.getInteger("skill_id");
        _level        = set.getInteger("level");

        _displayId    = set.getInteger("displayId", _id);
        _name         = set.getString("name");
        _operateType  = set.getEnum("operateType", SkillOpType.class);
        _magic        = set.getBool  ("isMagic", false);
        _ispotion     = set.getBool  ("isPotion", false);
        _mpConsume    = set.getInteger("mpConsume", 0);
        _mpInitialConsume = set.getInteger("mpInitialConsume", 0);
        _hpConsume    = set.getInteger("hpConsume", 0);
        _itemConsume  = set.getInteger("itemConsumeCount", 0);
        _itemConsumeId= set.getInteger("itemConsumeId", 0);
        _itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
        _itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
        _itemConsumeTime = set.getInteger("itemConsumeTime", 0);
        _iRate        = set.getInteger("iRate", 0);
        _iKill        = set.getBool  ("iKill", false);
        _castRange    = set.getInteger("castRange", 0);
        _effectRange = set.getInteger("effectRange", -1);
        _skillTime    = set.getInteger("skillTime", 0);
        _skillInterruptTime = set.getInteger("skillTime", _skillTime/2);
        _hitTime      = set.getInteger("hitTime", 0);
        _reuseDelay   = set.getInteger("reuseDelay", 0);
        _isDance      = set.getBool("isDance",false);        
        if(_isDance)
        {
            _buffDuration = set.getInteger("buffDuration", 1) * Config.ALT_DANCE_TIME;}
        else {
            _buffDuration = set.getInteger("buffDuration", 1) * Config.ALT_BUFF_TIME;
        }
        
        _skillRadius  = set.getInteger("skillRadius", 80);
        
        _targetType   = set.getEnum("target", SkillTargetType.class);
        _power        = set.getFloat("power", 0.f);
        _negateStats = set.getString("negateStats", "").split(" ");
        _negatePower = set.getFloat("negatePower", 0.f);
        _levelDepend  = set.getInteger("lvlDepend", 0);
        _stat         = set.getEnum  ("stat", Stats.class, null);
        
        _skillType    = set.getEnum("skillType", SkillType.class);
        _effectType   = set.getEnum("effectType", SkillType.class, null);
        _effectPower  = set.getInteger("effectPower", 0);
        _effectLvl = set.getInteger("effectLevel", 0);
        _skill_landing_percent = set.getInteger("skill_landing_percent", 0);
        _element      = set.getInteger("element", 0);
        _savevs       = set.getInteger("save", 0);
        _activateRate = set.getInteger("activateRate", -1);
        _levelModifier= set.getInteger("levelModifier", 1);
        _magicLevel   = set.getInteger("magicLvl", SkillTreeTable.getInstance().getMinSkillLevel(_id, _level));
        
        _ignoreShld       = set.getBool  ("ignoreShld", false);
        _critical       = set.getBool  ("critcal", false);
        _condition       = set.getInteger("condition", 0);
        _conditionValue  = set.getInteger("conditionValue", 0);
        _overhit         = set.getBool  ("overHit", false);
        _isSuicideAttack = set.getBool("isSuicideAttack", false);
        _weaponsAllowed  = set.getInteger("weaponsAllowed", 0);
        _armorsAllowed   = set.getInteger("armorsAllowed", 0);

        _addCrossLearn       = set.getInteger("addCrossLearn", 1000);
        _mulCrossLearn       = set.getFloat ("mulCrossLearn", 2.f);
        _mulCrossLearnRace   = set.getFloat ("mulCrossLearnRace", 2.f);
        _mulCrossLearnProf   = set.getFloat ("mulCrossLearnProf", 3.f);
        _isOffensive         = set.getBool  ("offensive",isSkillTypeOffensive());
        _num_charges = set.getInteger("num_charges", getLevel());
        _successRate         = set.getFloat("rate", 1);
        _minPledgeClass     = set.getInteger("minPledgeClass", 0);

        int l1 = set.getInteger("lethal1",0);
        int l2 = set.getInteger("lethal2",0);
        if( l1 <= l2 || l2 <= 0)
        {
           _lethalEffect1 = 0;
           _lethalEffect2 = 0;
        }
        else
        {
           _lethalEffect1 = l1;
           _lethalEffect2 = l2;
        }

        _directHpDmg  = set.getBool("dmgDirectlyToHp",false);
        _nextDanceCost = set.getInteger("nextDanceCost", 0);

        String canLearn = set.getString("canLearn", null);
        if (canLearn == null)
        {
            _canLearn = null;
        } 
        else 
        {
            _canLearn = new FastList<ClassId>();
            StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");
            while (st.hasMoreTokens()) 
            {
                String cls = st.nextToken();
                try 
                {
                    _canLearn.add(ClassId.valueOf(cls));
                }
                catch (Throwable t)
                {
                    _log.fatal( "Bad class "+cls+" to learn skill", t);
                }
            }
        }

        String teachers = set.getString("teachers", null);
        if (teachers == null)
        {
            _teachers = null;
        } 
        else 
        {
            _teachers = new FastList<Integer>();
            StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
            while (st.hasMoreTokens())
            {
                String npcid = st.nextToken();
                try
                {
                    _teachers.add(Integer.parseInt(npcid));
                }
                catch (Throwable t)
                {
                    _log.fatal( "Bad teacher id "+npcid+" to teach skill", t);
                }
            }
        }
    }
    
    public abstract void useSkill(L2Character caster, L2Object[] targets);
    

    public final boolean isPotion()
    {
        return _ispotion;
    }

    public final int getArmorsAllowed()
    {
        return _armorsAllowed;
    }
    
    public final int getConditionValue()
    {
        return _conditionValue;
    }
    
    public final SkillType getSkillType() 
    {
        return _skillType;
    }
    
    public final int getSavevs() {
        return _savevs;
    }

    public final int getActivateRate() {
        return _activateRate;
    }

    public final int getLevelModifier() {
        return _levelModifier;
    }

    public final int getMagicLevel() {
        return _magicLevel;
    }
    
    public final int getElement() 
    {
        return _element;
    }
    
    /**
     * Return the target type of the skill : SELF, PARTY, CLAN, PET...<BR><BR>
     * 
     */
    public final SkillTargetType getTargetType() 
    {
        return _targetType;
    }
    
    public final int getCondition() {
        return _condition;
    }
    
    public final boolean isCritical() 
    {
        return _critical;
    }
    public final boolean ignoreShld()
    {
        return _ignoreShld;
    }
    public final boolean isOverhit() 
    {
        return _overhit;
    }

    public final boolean isSuicideAttack()
    {
         return _isSuicideAttack;
    }

    /** INSTANT KILL **/
    public final boolean isInstantKill() 
    {
        return _iKill;
    }
    /** Return the rate in Perecent% of chance to InstantKill**/
    public final int getInstantKillRate() 
    {
        return _iRate;
    }
    
    /**
     * Return the power of the skill.<BR><BR>
     */
    public final double getPower(L2Character activeChar)
    {
         //Use another formula for Curse Death Link because default formula is very weak
        /*if (_skillType == SkillType.DEATHLINK && activeChar != null)
            return _power * Math.pow(1.7165 - activeChar.getCurrentHp()/activeChar.getMaxHp(), 2) * 0.577;
        else return _power;
        */
        if (_skillType != SkillType.DEATHLINK)
        return _power;
        else
        {
        if (activeChar.getCurrentHp() >= activeChar.getMaxHp()*0.29)
            return _power * 1.1;
        else if (activeChar.getCurrentHp() >= activeChar.getMaxHp()*0.26)
            return _power * 1.19;
        else if (activeChar.getCurrentHp() >= activeChar.getMaxHp()*0.23)
            return _power * 1.215;
        else if (activeChar.getCurrentHp() >= activeChar.getMaxHp()*0.21)
            return _power * 1.22;
        else if (activeChar.getCurrentHp() >= activeChar.getMaxHp()*0.16)
            return _power * 1.23;
        else if (activeChar.getCurrentHp() >= activeChar.getMaxHp()*0.13)
            return _power * 1.24;
        else if (activeChar.getCurrentHp() >= activeChar.getMaxHp()*0.09)
            return _power * 1.35;
        else if (activeChar.getCurrentHp() <= 300)
            return _power * 1.8;
        else if (activeChar.getCurrentHp() <= 200)
            return _power * 2.1;
        else if (activeChar.getCurrentHp() <= 170)
            return _power * 2.2;
        else if (activeChar.getCurrentHp() <= 150)
            return _power * 2.3;
        else if (activeChar.getCurrentHp() <= 130)
            return _power * 2.5;
        else if (activeChar.getCurrentHp() <= 100)
            return _power * 2.8;
        }
        return _power;
    }
    
    public final double getPower()
    {
        return _power;
    }
    
    public final String[] getNegateStats()
    {
        return _negateStats;
    }
    
    public final float getNegatePower()
    {
        return _negatePower;
    }
    
    public final int getMagicLvl()
    {
        return _magicLevel;
    }

    public final int getLevelDepend()
    {
        return _levelDepend;
    } 
             
    /** 
    * Return the skill landing percent probability.<BR><BR> 
    */ 
    public final int getLandingPercent() 
    { 
        return _skill_landing_percent; 
    } 
    
    /** 
     * Return the additional effect power or base probability.<BR><BR> 
     */ 
     public final int getEffectPower() 
     { 
         return _effectPower; 
     } 
     
     /**
      * Return the additional effect level.<BR><BR>
      */
     public final int getEffectLvl()
     {
         return _effectLvl;
     }
    
    /** Return the additional effect skill type (ex : STUN, PARALYZE,...).<BR><BR> 
    */ 
    public final SkillType getEffectType()  
    { 
        return _effectType;
    }
    
    /**
     * @return Returns the buffDuration.
     */
    public final int getBuffDuration()
    {
        return _buffDuration;
    }

    /**
     * @return Returns the castRange.
     */
    public final int getCastRange()
    {
        return _castRange;
    }

    /**
     * @return Returns the effectRange.
     */
    public final int getEffectRange()
    {
        return _effectRange;
    }

    /**
     * @return Returns the hitTime.
     */
    public final int getHitTime()
    {
        return Math.round(Config.ALT_GAME_SKILL_HIT_RATE * _hitTime);
    }

    /**
     * @return Returns the hpConsume.
     */
    public final int getHpConsume()
    {
        return _hpConsume;
    }

    /**
     * @return Returns the id.
     */
    public final int getId()
    {
        return _id;
    }

    public int getDisplayId()
    {
        return _displayId;
    }

    public void setDisplayId(int id)
    {
        _displayId = id;
    }

    public float getSuccessRate()
    {
        return _successRate;
    }

    public int getMinPledgeClass()
    {
        return _minPledgeClass;
    }


    /**
     * Return the skill type (ex : BLEED, SLEEP, WATER...).<BR><BR>
     */
    public final Stats getStat()
    {
        return _stat;
    }
    
    /**
     * @return Returns the itemConsume.
     */
    public final int getItemConsume()
    {
        return _itemConsume;
    }

    /**
     * @return Returns the itemConsumeId.
     */
    public final int getItemConsumeId()
    {
        return _itemConsumeId;
    }
    
    /**
     * @return Returns the itemConsume count over time.
     */
    public final int getItemConsumeOT()
    {
        return _itemConsumeOT;
    }
    
    /**
     * @return Returns the itemConsumeId over time.
     */
    public final int getItemConsumeIdOT()
    {
        return _itemConsumeIdOT;
    }
    
    /**
     * @return Returns the itemConsume time in milliseconds.
     */
    public final int getItemConsumeTime()
    {
        return _itemConsumeTime;
    }

    /**
     * @return Returns the level.
     */
    public final int getLevel()
    {
        return _level;
    }

    /**
     * @return Returns the magic.
     */
    public final boolean isMagic()
    {
        return _magic;
    }

    /**
     * @return Returns the mpConsume.
     */
    public final int getMpConsume()
    {
        return _mpConsume;
    }
    
    /**
     * @return Returns the mpInitialConsume.
     */
    public final int getMpInitialConsume()
    {
    	return _mpInitialConsume;
    }

    /**
     * @return Returns the name.
     */
    public final String getName()
    {
        return _name;
    }

    /**
     * @return Returns the reuseDelay.
     */
    public final int getReuseDelay()
    {
        return _reuseDelay;
    }

    public final int getSkillTime()
    {
        return _skillTime;
    }

    public final int getSkillInterruptTime()
    {
        return _skillInterruptTime;
    }
    
    public final int getSkillRadius()
    {
        return _skillRadius;
    }

    public final boolean isActive()
    {
        return _operateType == SkillOpType.OP_ACTIVE;
    }
    
    public final boolean isPassive()
    {
        return _operateType == SkillOpType.OP_PASSIVE;
    }
    
    public final boolean isToggle()
    {
        return _operateType == SkillOpType.OP_TOGGLE;
    }

    public final boolean isDance()
    {
        return _isDance;
    }
    
    public final int getNextDanceMpCost()
    {
       return _nextDanceCost;
    }

    public final boolean useSoulShot()
    {
        return ((getSkillType() == SkillType.PDAM)||
                (getSkillType() == SkillType.STUN)||
                (getSkillType() == SkillType.CRITS)||
                (getSkillType() == SkillType.CHARGEDAM));
    }
    
    public final boolean useSpiritShot()
    {
        return isMagic();
    }

    public final boolean useFishShot()
    {
       return ((getSkillType() == SkillType.PUMPING) || (getSkillType() == SkillType.REELING) );
    }

    public final int getWeaponsAllowed()
    {
        return _weaponsAllowed;
    }
    
    public final int getCrossLearnAdd() {
        return _addCrossLearn;
    }
    public final float getCrossLearnMul() {
        return _mulCrossLearn;
    }
    public final float getCrossLearnRace() {
        return _mulCrossLearnRace;
    }
    public final float getCrossLearnProf() {
        return _mulCrossLearnProf;
    }
    public final boolean getCanLearn(ClassId cls) {
        return _canLearn == null || _canLearn.contains(cls);
    }
    public final boolean canTeachBy(int npcId) {
        return _teachers == null || _teachers.contains(npcId);
    }
    public final boolean isPvpSkill() {
        switch (_skillType)
        {
        case DOT:
        case BLEED:
        case CONFUSION:
        case POISON:
        case DEBUFF:
        case AGGDEBUFF:
        case STUN:
        case ROOT:
        case FEAR:
        case SLEEP:
        case MDOT:
        case MANADAM:
        case MUTE:
        case WEAKNESS:
        case PARALYZE:
        case CANCEL:
        case MAGE_BANE:
        case WARRIOR_BANE:            
        case CANCEL_TARGET:
        case BETRAY:
            return true;
        default:
            return false;
        }
    }

    public final boolean isOffensive()
    {
        return _isOffensive;
    }

    public final int getLethalChance1()
    {
        return _lethalEffect1;
    }

    public final int getLethalChance2()
    {
        return _lethalEffect2;
    }

    public final boolean getDmgDirectlyToHP()
    {
       return _directHpDmg;
    }

    public final boolean isSkillTypeOffensive()
    {
        switch (_skillType)
        {
            case PDAM:
            case MDAM:
            case DOT:
            case BLEED:
            case POISON:
            case AGGDAMAGE:
            case DEBUFF:
            case AGGDEBUFF:
            case STUN:
            case ROOT:
            case CONFUSION:
            case UNLOCK:
            case ERASE:
            case FEAR:
            case DRAIN:
            case SLEEP:
            case CHARGEDAM:
            case CONFUSE_MOB_ONLY:
            case DEATHLINK:
            case DETECT_WEAKNESS:
            case MDOT:
            case MANADAM:
            case MUTE:
            case SOULSHOT:
            case SPIRITSHOT:
            case SPOIL:
            case WEAKNESS:
            case MANA_BY_LEVEL:
            case SWEEP:
            case PARALYZE:
            case DRAIN_SOUL:
            case AGGREDUCE:
            case CANCEL:
            case MAGE_BANE:
            case WARRIOR_BANE:                
            case AGGREMOVE:
            case AGGREDUCE_CHAR: 
            case CRITS:
            case CHARGE_SELF:
            case SUMMON:  
            case UNSUMMON_ENEMY_PET:
            case CANCEL_TARGET:
            case BETRAY:
                return true;
            default:
                return false;
        }
    }

    public final boolean isPositive() {
        switch (_skillType)
        {
              case BUFF:
              case HEAL:
              case HEAL_PERCENT:
              case HOT:
              case MANAHEAL:
              case MANARECHARGE:
              case NEGATE:
              case CANCEL:
              case REFLECT:
              case UNBLEED:
              case UNPOISON:
              case SEED:
            return true;
        default:
            return false;
        }
    }

    public final boolean isNeedWeapon() { return (_skillType == SkillType.MDAM) ? true : false; }
    
//  int weapons[] = {L2Weapon.WEAPON_TYPE_ETC, L2Weapon.WEAPON_TYPE_BOW,
//           L2Weapon.WEAPON_TYPE_POLE, L2Weapon.WEAPON_TYPE_DUALFIST,
//           L2Weapon.WEAPON_TYPE_DUAL, L2Weapon.WEAPON_TYPE_BLUNT,
//           L2Weapon.WEAPON_TYPE_SWORD, L2Weapon.WEAPON_TYPE_DAGGER};
    
    public final boolean getWeaponDependancy(L2Character activeChar)
    {
        int weaponsAllowed = getWeaponsAllowed();
        //check to see if skill has a weapon dependency.
        if(weaponsAllowed == 0)
            return true;
        if(activeChar.getActiveWeaponItem() != null)
        {
            L2WeaponType playerWeapon;
            playerWeapon = activeChar.getActiveWeaponItem().getItemType();
            int mask = playerWeapon.mask(); 
            if((mask & weaponsAllowed) != 0)
                return true;
            // can be on the secondary weapon
            if (activeChar.getSecondaryWeaponItem() != null)
            {
                playerWeapon = activeChar.getSecondaryWeaponItem().getItemType();
                mask = playerWeapon.mask();
                if((mask & weaponsAllowed) != 0)
                    return true;
            }
        }
        TextBuilder skillmsg = new TextBuilder();
        skillmsg.append(getName());
        skillmsg.append(" can only be used with weapons of type ");
        for(L2WeaponType wt : L2WeaponType.values())
        {
            if((wt.mask() & weaponsAllowed) != 0)
                skillmsg.append(wt).append('/');
        }
        skillmsg.setCharAt(skillmsg.length()-1, '.');
        SystemMessage message = new SystemMessage(SystemMessage.S1_S2);
        message.addString(skillmsg.toString()); 
        activeChar.sendPacket(message); 
        
        return false;
    }
    
    public boolean checkCondition(L2Character activeChar, boolean itemOrWeapon)
    {
        if((getCondition() & L2Skill.COND_BEHIND) != 0)
        {
            if (!activeChar.isBehindTarget())
            return false;
        }
        
        if((getCondition() & L2Skill.COND_SHIELD) != 0)
        {
            /*
            L2Armor armorPiece;
            L2ItemInstance dummy;
            dummy = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
            armorPiece = (L2Armor) dummy.getItem();
            */
            //TODO add checks for shield here.
            
        }

        Condition preCondition = _preCondition;
        if(itemOrWeapon) preCondition = _itemPreCondition;

        if (preCondition == null) return true;
        Env env = new Env();
        env._player = activeChar;
        env._skill = this;
        if (!preCondition.test(env))
        {
            String msg = preCondition.getMessage();
            if (msg != null)
            {
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString(msg);
                activeChar.sendPacket(sm);
            }
            return false;
        }
        return true;
    }
    
    
    /**
     * Return all targets of the skill in a table in function a the skill type.<BR><BR>
     * 
     * <B><U> Values of skill type</U> :</B><BR><BR>
     * <li>ONE : The skill can only be used on the L2PcInstance targeted, or on the caster if it's a L2PcInstance and no L2PcInstance targeted</li>
     * <li>SELF</li>
     * <li>HOLY, UNDEAD</li>
     * <li>PET</li>
     * <li>AURA, AURA_CLOSE</li>
     * <li>AREA</li>
     * <li>MULTIFACE</li>
     * <li>PARTY, CLAN</li>
     * <li>CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN</li>
     * <li>UNLOCKABLE</li>
     * <li>ITEM</li><BR><BR>
     * 
     * @param activeChar The L2Character who use the skill
     * 
     */
    public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst)
    {
        List<L2Character> targetList = new FastList<L2Character>();
        
        // Get the target type of the skill 
        // (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
        SkillTargetType targetType = getTargetType();
        
        // Init to null the target of the skill
        L2Character target = null;
        
        // Get the L2Objcet targeted by the user of the skill at this moment
        L2Object objTarget = activeChar.getTarget();
        
        // Get the type of the skill
        // (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
        SkillType skillType = getSkillType();

        // If the L2Object targeted is a L2Character, it becomes the L2Character target
        if(objTarget instanceof L2Character)
        {
            target = (L2Character) objTarget;
        }
        
        
        switch(targetType)
        {
        // The skill can only be used on the L2Character targeted, or on the caster itself
        case TARGET_ONE:
        {
            // automaticly selects caster if no target is selected (only positive skills)  
            if (isPositive() && target == null)  
                    target = activeChar;  
            // Check for null target or any other invalid target
            if(target == null || target.isDead() || 
            (target == activeChar && !(
                    skillType == SkillType.BUFF
                    || skillType == SkillType.HEAL
                    || skillType == SkillType.HEAL_PERCENT
                    || skillType == SkillType.HOT
                    || skillType == SkillType.MANAHEAL
                    || skillType == SkillType.MANARECHARGE
                    || skillType == SkillType.NEGATE
                    || skillType == SkillType.CANCEL
                    || skillType == SkillType.REFLECT
                    || skillType == SkillType.UNBLEED
                    || skillType == SkillType.UNPOISON
                    || skillType == SkillType.SEED
                    || skillType == SkillType.COMBATPOINTHEAL                    
                    || skillType == SkillType.MAGE_BANE 
                    || skillType == SkillType.WARRIOR_BANE
                    )))
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }
            if (target instanceof L2Attackable && (skillType == SkillType.BUFF
                    || skillType == SkillType.HEAL
                    || skillType == SkillType.HEAL_PERCENT
                    || skillType == SkillType.HOT
                    || skillType == SkillType.MANAHEAL
                    || skillType == SkillType.MANARECHARGE
                    || skillType == SkillType.NEGATE
                    || skillType == SkillType.CANCEL
                    || skillType == SkillType.REFLECT
                    || skillType == SkillType.UNBLEED
                    || skillType == SkillType.UNPOISON
                    )) {
                activeChar.startPvPFlag();
                activeChar.setlastPvpAttack(System.currentTimeMillis());}

            if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                    return null;
                }   
       
            // If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
            return new L2Character[]{target};
            //}
        }
        case TARGET_SELF:
        {     
                    return new L2Character[]{activeChar};
        }
        /*case TARGET_BOSS:
        {
            L2MinionInstance Minion = null;
            Minion = (L2MinionInstance)target;
            if (activeChar instanceof L2MinionInstance)
             return new L2Character[]{target};
        }*/
        case TARGET_HOLY:
        {
            if (activeChar instanceof L2PcInstance && activeChar.getTarget() instanceof L2ArtefactInstance)
            {
                if ( Config.ALLOW_GEODATA)
                    if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, activeChar.getTarget()) == false)
                    {
                        activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                        return null;
                    }
                    return new L2Character[]{(L2ArtefactInstance)activeChar.getTarget()};
            }

            return null;
        }
        case TARGET_PET:
        {
            target = activeChar.getPet();
            if (target != null && !target.isDead())
                return new L2Character[]{target}; 
            
            return null;
        }
        case TARGET_OWNER_PET:
        {
           if (activeChar instanceof L2Summon)
           {
               target = ((L2Summon)activeChar).getOwner();
               if (target != null && !target.isDead())
                   return new L2Character[]{target}; 
            }
           
            return null;
        }
        case TARGET_ENEMY_PET:
        {
            if(target != null && target instanceof L2Summon)
            {      
                L2Summon targetPet = null;
                targetPet = (L2Summon)target;
                if (activeChar instanceof L2PcInstance && activeChar.getPet() != targetPet 
                    && !targetPet.isDead() && targetPet.getOwner().getPvpFlag() != 0)
                {
                    if ( Config.ALLOW_GEODATA)
                        if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                        {
                            activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                            return null;
                        }   
                   return new L2Character[]{target};
                }
            }
            return null;
        }
        case TARGET_CORPSE_PET:
        {
            if (activeChar instanceof L2PcInstance)
            {
                target = activeChar.getPet();
                if (target != null && target.isDead())
                {
                    if ( Config.ALLOW_GEODATA)
                        if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                        {
                            activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                            return null;
                        }   
                    return new L2Character[]{target}; 
                }
            }
            
            return null;
        }   
        case TARGET_AURA_UNDEAD:
        case TARGET_AURA:
        {
            int radius = getSkillRadius();
            boolean srcInArena = (ArenaManager.getInstance().getArena(activeChar) != null);

            L2PcInstance src = null;
            if (activeChar instanceof L2PcInstance) src = (L2PcInstance)activeChar;
            if (activeChar instanceof L2Summon) src = ((L2Summon)activeChar).getOwner();
            
            // Go through the L2Character _knownList
            for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
            {
                if (obj != null && (obj instanceof L2Attackable || obj instanceof L2PlayableInstance))
                {
                    // Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
                    if (obj == activeChar || obj == src) continue;
                    if (src != null) 
                    {
                        // check if both attacker and target are L2PcInstances and if they are in same party 
                        if (obj instanceof L2PcInstance) 
                        {
                            if(!src.checkPvpSkill(obj, this)) continue;
                            if((src.getParty() != null && ((L2PcInstance) obj).getParty() != null) && src.getParty().getPartyLeaderOID() == ((L2PcInstance) obj).getParty().getPartyLeaderOID())
                                continue;
                            if(!srcInArena && ArenaManager.getInstance().getArena(obj) == null)
                            {
                                if(src.getClanId() != 0 && src.getClanId() == ((L2PcInstance)obj).getClanId())
                                    continue;
                            }
                        }
                        if(obj instanceof L2Summon)
                        {
                            L2PcInstance trg = ((L2Summon)obj).getOwner();
                            if(trg == src) continue;
                            if(!src.checkPvpSkill(trg, this)) continue;
                            if((src.getParty() != null && trg.getParty() != null) && 
                                src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) 
                                continue;
                            if(!srcInArena && ArenaManager.getInstance().getArena(obj) == null)
                            {
                                if(src.getClanId() != 0 && src.getClanId() == trg.getClanId())
                                    continue;
                            }
                        }
                        if(targetType == SkillTargetType.TARGET_AURA_UNDEAD && !obj.isUndead())
                            continue;
                        if ( Config.ALLOW_GEODATA)
                            if (GeoDataRequester.getInstance().hasAttackLoS(src, obj) == false)
                                continue;
                    }
                    
                    if (onlyFirst == false) targetList.add(obj);
                    else return new L2Character[] {obj};
                }
            }
            return targetList.toArray(new L2Character[targetList.size()]);
        }
        case TARGET_AREA:
        {
            if ((!(target instanceof L2Attackable || target instanceof L2PlayableInstance)) ||  //   Target is not L2Attackable or L2PlayableInstance
                (this.getCastRange() >= 0 && (target == null || target == activeChar || target.isAlikeDead()))) //target is null or self or dead/faking
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }

            L2Character cha;
            
            if (getCastRange() >= 0)
            {
                if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                    if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                    {
                        activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                        return null;
                    }   
                
                cha = target;
                
                if(!onlyFirst) targetList.add(cha); // Add target to target list
                else return new L2Character[]{cha};
            }
            else cha = activeChar;
            
            boolean effectOriginIsL2PlayableInstance = (cha instanceof L2PlayableInstance);

            L2PcInstance src = null;
            if (activeChar instanceof L2PcInstance) src = (L2PcInstance)activeChar;
            else if (activeChar instanceof L2Summon) src = ((L2Summon)activeChar).getOwner();

            boolean srcInArena = (ArenaManager.getInstance().getArena(activeChar)!= null);
            
            for (L2Character obj : cha.getKnownList().getKnownCharactersInRadius(getSkillRadius()))
            {
                if (obj == null || obj == cha || obj == activeChar)
                    continue;
                
                if(!obj.isAlikeDead())   
                {
                    if (src != null) // caster is l2playableinstance and exists
                    {
                        if(obj instanceof L2PcInstance)
                        { 
                            L2PcInstance trg = (L2PcInstance)obj;
                            
                            if((src.getParty() != null && trg.getParty() != null) && 
                                src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) 
                                continue;
                            
                            if(!srcInArena && ArenaManager.getInstance().getArena(trg) == null)
                            {
                                if(src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0) 
                                    continue;
                                
                                if(ZoneManager.getInstance().checkIfInZonePeace(obj)) continue;
                                
                                if(src.getClan() != null && trg.getClan() != null)
                                {
                                    if(src.getClan().getClanId() == trg.getClan().getClanId()) 
                                      continue;
                                }
                                   
                                if(!src.checkPvpSkill(obj, this)) 
                                    continue;
                            }
                        }
                        if(obj instanceof L2Summon)
                        {
                            L2PcInstance trg = ((L2Summon)obj).getOwner();
                            
                            if((src.getParty() != null && trg.getParty() != null) && 
                                    src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) 
                                continue;
                             
                            if(!srcInArena && ArenaManager.getInstance().getArena(trg) == null)
                            {
                                if(src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0) 
                                    continue;
                             
                                if(ZoneManager.getInstance().checkIfInZonePeace(obj)) continue;
                             
                                if(src.getClan() != null && trg.getClan() != null)
                                {
                                    if(src.getClan().getClanId() == trg.getClan().getClanId()) 
                                        continue;
                                }
                             
                                	if(!src.checkPvpSkill(trg, this)) 
                                    continue;
                            
                            }
                        }
                        
                        if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                            if (GeoDataRequester.getInstance().hasAttackLoS(cha, obj) == false)
                                continue;
                    }
                    else
                    // Skill user is not L2PlayableInstance
                    {
                        if (effectOriginIsL2PlayableInstance && // If effect starts at L2PlayableInstance and
                                !(obj instanceof L2PlayableInstance)) // Object is not L2PlayableInstance
                                continue;
                    }
                    
                    targetList.add(obj);
                }
            }
            
            if (targetList.size() == 0)
                return null;
            
            return targetList.toArray(new L2Character[targetList.size()]);
        }
        case TARGET_MULTIFACE:
        {
            if((!(target instanceof L2Attackable)
                    && !(target instanceof L2PcInstance))
                    || target == activeChar)
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }
            
            if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                    return null;
                }   
            

                if (!onlyFirst) targetList.add(target);
                else return new L2Character[] {target};

            int radius = getSkillRadius();
            
            for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
            {
                if (obj == null) continue;
                if(obj instanceof L2Attackable && obj != target)
                {
                    // Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
                    if (activeChar instanceof L2PcInstance && !((L2PcInstance)activeChar).checkPvpSkill(obj, this))
                        continue;
                    //Don't add this target,Mob skills Shouldn't affect other Mobs
                    if (activeChar instanceof L2MonsterInstance && !(obj instanceof L2MonsterInstance))
                        continue;
                    if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                        if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, obj) == false)
                            continue;
                    targetList.add(obj);
                }
                
                if(targetList.size() == 0)
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
                    return null;
                }
            }
            return targetList.toArray(new L2Character[targetList.size()]);
            //TODO multiface targets all around right now.  need it to just get targets
            //the character is facing.
        }
        case TARGET_PARTY:
        {
            if (onlyFirst)
                 return new L2Character[]{activeChar};
            
             targetList.add(activeChar);

             L2PcInstance player = null;
                
             if (activeChar instanceof L2Summon) 
             {
               player = ((L2Summon)activeChar).getOwner();
                 targetList.add(player);
             }
             else if (activeChar instanceof L2PcInstance
                       && activeChar.getPet() != null) 
             {
               player = (L2PcInstance)activeChar;
                 targetList.add(activeChar.getPet());
             }

             if (activeChar.getParty() != null)
             {
                 // Get all visible objects in a spheric area near the L2Character
                 // Get a list of Party Members
                 List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();
                     
                 for(L2PcInstance partyMember : partyList)
                 {
                     if (partyMember == null || partyMember == player) continue;
                     
                     if (partyMember.isDead()
                             || !Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true))
                         continue;
                     
                     if ( Config.ALLOW_GEODATA)
                         if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, partyMember) == false)
                             continue;
                     
                         targetList.add(partyMember);
                         if (partyMember.getPet() != null)
                         {
                             targetList.add(partyMember.getPet());
                         }
                 }
             }
             return targetList.toArray(new L2Character[targetList.size()]);
         }
         case TARGET_PARTY_MEMBER:
         {
             if ((target != null
                     && target == activeChar) 
                 || (target != null
                         && activeChar.getParty() != null
                         && target.getParty() != null
                         && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
                 || (target != null
                         && activeChar instanceof L2PcInstance
                         && target instanceof L2Summon
                         && activeChar.getPet() == target)
                 || (target != null
                         && activeChar instanceof L2Summon
                         && target instanceof L2PcInstance
                         && activeChar == target.getPet()))
             {
                 if (!target.isDead())
                 {
                     if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                         if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                         {
                             activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                             return null;
                         }
                     // If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
                     return new L2Character[]{target};
                 }
                 else
                     return null;
             }
             else 
             {
                 activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                 return null;
             }
        }
        case TARGET_CORPSE_ALLY:
        case TARGET_ALLY:
        {
            if(activeChar instanceof L2PcInstance)
            {
                int radius = getSkillRadius();
                L2PcInstance player = (L2PcInstance) activeChar;
                
                if (targetType == SkillTargetType.TARGET_ALLY)
                    targetList.add(player);
                
                if (player.getAllyId() == 0)
                {
                    L2Clan clan = player.getClan();
                    
                    if(clan != null)
                    {
                        // Get all visible objects in a spheric area near the L2Character
                        // Get Clan Members
                        for (L2ClanMember member : clan.getMembers())
                        {
                            L2PcInstance newTarget = member.getPlayerInstance();
                            if (newTarget == null || newTarget == activeChar)
                                continue;
                            if (targetType == SkillTargetType.TARGET_CORPSE_ALLY && !newTarget.isDead())
                                continue;
                            
                            if (!Util.checkIfInRange(radius, activeChar, newTarget, true)) continue;
                            
                            // Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
                            if (!player.checkPvpSkill(newTarget, this))
                                continue;
                            
                            if ( Config.ALLOW_GEODATA)
                                if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, newTarget) == false)
                                    continue;

                            targetList.add(newTarget);
                        }
                    }
                }
                else
                {
                    for ( L2PcInstance newTarget : activeChar.getKnownList().getKnownPlayersInRadius(radius))
                    {
                        if (newTarget == null || newTarget == activeChar)
                            continue;
                        if (newTarget.getAllyId() != player.getAllyId()) 
                            continue;
                        if (targetType == SkillTargetType.TARGET_CORPSE_ALLY && !newTarget.isDead())
                            continue;
                        
                        // Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
                        if (!player.checkPvpSkill(newTarget, this))
                            continue;
                        
                        if ( Config.ALLOW_GEODATA)
                            if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, newTarget) == false)
                                continue;

                        targetList.add(newTarget);
                    }
                }
                return targetList.toArray(new L2Character[targetList.size()]);
            }
        }
        case TARGET_ENEMY_ALLY:
        {
            //int charX, charY, charZ, targetX, targetY, targetZ, dx, dy, dz;
            int radius = getSkillRadius();
            L2Character newTarget;
            
            if (getCastRange() > -1 && target != null)
            {
                if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                    if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                    {
                        activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                        return null;
                    }
                newTarget = target;
            }
            else
                newTarget = activeChar;
            
            if (newTarget != activeChar || this.isSkillTypeOffensive())
                targetList.add(newTarget);
            
            for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
            {
                if (obj == newTarget || obj == activeChar)
                        continue;
                
                if (obj instanceof L2Attackable)
                {
                    if(!obj.isAlikeDead())
                    {
                        // Don't add this target if this is a PC->PC pvp casting and pvp condition not met
                        if (activeChar instanceof L2PcInstance && !((L2PcInstance)activeChar).checkPvpSkill(obj, this))
                            continue;
                        
                        // check if both attacker and target are L2PcInstances and if they are in same party or clan
                        if (   (activeChar instanceof L2PcInstance && obj instanceof L2PcInstance) && 
                             (((L2PcInstance)activeChar).getClanId() != ((L2PcInstance)obj).getClanId() ||
                             (((L2PcInstance)activeChar).getAllyId() != ((L2PcInstance)obj).getAllyId() &&
                             ((((L2PcInstance)activeChar).getParty() != null && ((L2PcInstance)obj).getParty() != null) &&
                             ((L2PcInstance)activeChar).getParty().getPartyLeaderOID() != ((L2PcInstance)obj).getParty().getPartyLeaderOID() ))))
                            continue;
                        
                        if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                            if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, obj) == false)
                                continue;
                        
                        targetList.add(obj);
                    }
                }
            }
        }
        case TARGET_CORPSE_CLAN:
        case TARGET_CLAN:
        {
            if(activeChar instanceof L2PcInstance)
            {
                int radius = getSkillRadius();
                L2PcInstance player = (L2PcInstance) activeChar;
                L2Clan clan = player.getClan();
                if (targetType == SkillTargetType.TARGET_CLAN)
                    targetList.add(player);
                if(clan != null)
                {
                    // Get all visible objects in a spheric area near the L2Character
                    // Get Clan Members
                    for (L2ClanMember member : clan.getMembers())
                    {
                        L2PcInstance newTarget = member.getPlayerInstance();
                        if (newTarget == null || newTarget == activeChar)
                            continue;
                        if (targetType == SkillTargetType.TARGET_CORPSE_CLAN && !newTarget.isDead())
                            continue;

                        if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
                            continue;
                        
                        // Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
                        if (!player.checkPvpSkill(newTarget, this))
                            continue;
                        
                        if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                            if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, newTarget) == false)
                                continue;
                        
                        targetList.add(newTarget);
                    }
                }
            }
            return targetList.toArray(new L2Character[targetList.size()]);
        }
        case TARGET_CORPSE_PLAYER:
        {
            if(target != null && target.isDead())
            {
                L2PcInstance player = null;
                   
                if (activeChar instanceof L2PcInstance) player = (L2PcInstance)activeChar;
                L2PcInstance targetPlayer = null;
                if (target instanceof L2PcInstance) targetPlayer = (L2PcInstance)target;
                L2PetInstance targetPet = null;
                if (target instanceof L2PetInstance) targetPet = (L2PetInstance)target;
                
                if(player != null && (targetPlayer != null || targetPet != null))
                {
                    boolean condGood = true;
                    
                    if (getId() == 1016)     // Greater Resurrection
                    {

                        // check target is not in a active siege zone
                        Castle castle = null;
                        if (targetPlayer != null)
                                        castle = CastleManager.getInstance().getCastle(targetPlayer.getX(),targetPlayer.getY());
                        else if (targetPet != null)
                                        castle = CastleManager.getInstance().getCastle(targetPet.getX(),targetPet.getY());
                                    
                        if (castle != null)
                            if (castle.getSiege().getIsInProgress())
                            {
                                condGood = false;
                                player.sendPacket(new SystemMessage(SystemMessage.CANNOT_BE_RESURRECTED_DURING_SIEGE));
                            }

                        // Can only res party memeber or own pet
                        if (targetPlayer != null)
                        {
                           if (targetPlayer.isReviveRequested())
                           {
                               if (targetPlayer.isRevivingPet())
                                   player.sendPacket(new SystemMessage(1511)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
                               else
                                   player.sendPacket(new SystemMessage(1513)); // Resurrection is already been proposed.
                                condGood = false;
                           }
                        }
                        else if (targetPet != null)
                        {
                            if (targetPet.getOwner() != player)
                            {
                                condGood = false;
                                player.sendMessage("You are not the owner of this pet");
                            }
                        }
                    }
                    
                    if ( Config.ALLOW_GEODATA)
                        if (GeoDataRequester.getInstance().hasAttackLoS(player, target) == false)
                        {
                            player.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                            return null;
                        }

                    if (condGood)
                                return new L2Character[]{target};
                }
            }
            activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
            return null;
        }
        case TARGET_AREA_CORPSE_MOB:
        {
            if ((!(target instanceof L2Attackable)) || !target.isDead())
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }

            if (onlyFirst == false) targetList.add(target);
            else return new L2Character[] {target};
                
            boolean srcInArena = (ArenaManager.getInstance().getArena(activeChar)!= null);
            L2PcInstance src = null;
            if (activeChar instanceof L2PcInstance)
               src = (L2PcInstance)activeChar;
            L2PcInstance trg = null;

            int radius = getSkillRadius();
            if (activeChar.getKnownList() != null)
                for (L2Object obj : activeChar.getKnownList().getKnownObjects())
                {
                    if (!(obj instanceof L2Attackable) || ((L2Character) obj).isDead()
                        || ((L2Character) obj) == activeChar) continue;

                    if (!Util.checkIfInRange(radius, target, obj, true)) continue;
                        
                    if(obj instanceof L2PcInstance && src != null)
                    { 
                       trg = (L2PcInstance)obj;
                                                
                       if((src.getParty() != null && trg.getParty() != null) && 
                               src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) 
                           continue;
                       
                       if(!srcInArena && ArenaManager.getInstance().getArena(trg) == null)
                       {
                           if(src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0) 
                               continue;
                           
                           if(ZoneManager.getInstance().checkIfInZonePeace(obj)) continue;
                           
                           if(src.getClan() != null && trg.getClan() != null)
                           {
                               if(src.getClan().getClanId() == trg.getClan().getClanId()) 
                                   continue;
                           }
                           
                           if(!src.checkPvpSkill(obj, this)) 
                               continue;
                       }
                    }
                    if(obj instanceof L2Summon && src != null)
                    {
                       trg = ((L2Summon)obj).getOwner();
                       
                       if((src.getParty() != null && trg.getParty() != null) && 
                               src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) 
                           continue;
                       
                       if(!srcInArena && ArenaManager.getInstance().getArena(trg) == null)
                       {
                           if(src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0) 
                               continue;
                           
                           if(ZoneManager.getInstance().checkIfInZonePeace(obj)) continue;
                           
                           if(src.getClan() != null && trg.getClan() != null)
                           {
                               if(src.getClan().getClanId() == trg.getClan().getClanId()) 
                                   continue;
                           }
                           
                           if(!src.checkPvpSkill(trg, this)) 
                               continue;
                           
                       }
                    }

                    targetList.add((L2Character) obj);
                }

            if (targetList.size() == 0) return null;
            return targetList.toArray(new L2Character[targetList.size()]);
        }
        case TARGET_CORPSE_MOB:
        {
            if(!(target instanceof L2Attackable) || !target.isDead())
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }
            if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                    return null;
                }   
                            
                    return new L2Character[]{target};
                
        }
        case TARGET_MOB:
        {
            if(!(target instanceof L2Attackable))
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }
            if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                    return null;
                }   
                            
                    return new L2Character[]{target};
                
        }
        case TARGET_AREA_MOB:
        {
            if ((!(target instanceof L2Attackable)))
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }

            if (onlyFirst == false) targetList.add(target);
            else return new L2Character[] {target};

            int radius = getSkillRadius();
            if (target.getKnownList() != null)
                for (L2Object obj : target.getKnownList().getKnownObjects())
                {
                    if (!(obj instanceof L2Attackable) || ((L2Character) obj) == activeChar)
                        continue;

                    if (!Util.checkIfInRange(radius, target, obj, true)) continue;

                    targetList.add((L2Character) obj);
                }

            if (targetList.size() == 0) return null;
            return targetList.toArray(new L2Character[targetList.size()]);
        }
        case TARGET_GATE:
        {
                           // Check for null target or any other invalid target
                           if (target == null
                               || target.isDead()
                               || !(target instanceof L2DoorInstance))
                           {
                               activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                               return null;
                           }
           
                           // If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
                           return new L2Character[] {target};
        } 
        case TARGET_UNLOCKABLE:
        {
            if (!(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance)) 
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }
                
               return new L2Character[]{target};
        }       
        case TARGET_ITEM:
        {
            SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
            sm.addString("Target type of skill is not currently handled");
            activeChar.sendPacket(sm);
            return null;
        }
        case TARGET_UNDEAD:
        {
            if (target instanceof L2NpcInstance)
            {
                if(!(((L2NpcInstance)target).isUndead() && !(target.isDead()) ))
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                    return null;
                }
                if ( Config.ALLOW_GEODATA && activeChar instanceof L2PcInstance)
                    if (GeoDataRequester.getInstance().hasAttackLoS(activeChar, target) == false)
                    {
                        activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
                        return null;
                    }   

                   return new L2Character[]{target};
                                       
            } 
            else
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }
        }
        case TARGET_AREA_UNDEAD:
        {
            L2Character cha;
            if (this.getCastRange() >= 0)
            {
                cha = target;

                if (onlyFirst == false) targetList.add(cha); // Add target to target list
                else return new L2Character[] {cha};

            }
            else cha = activeChar;

            int radius = getSkillRadius();
            if (cha != null && cha.getKnownList() != null)
                for (L2Object obj : cha.getKnownList().getKnownObjects())
                {
                    if (obj == null) continue;
                        if (!(obj instanceof L2NpcInstance)) continue;
                    target = (L2NpcInstance) obj;
                    if (!target.isAlikeDead()) // If target is not dead/fake death and not self
                    {
                        if (!target.isUndead()) continue;
                        if (!Util.checkIfInRange(radius, cha, obj, true)) // Go to next obj if obj isn't in range
                            continue;

                        if (onlyFirst == false) targetList.add((L2Character) obj); // Add obj to target lists
                        else return new L2Character[] {(L2Character) obj};
                    }
                }

            if (targetList.size() == 0) return null;
            return targetList.toArray(new L2Character[targetList.size()]);
        }
        case TARGET_ENEMY_SUMMON: 
        { 
            if(target != null && target instanceof L2Summon) 
            {       
                L2Summon targetSummon = null; 
                targetSummon = (L2Summon)target; 
                if (activeChar instanceof L2PcInstance && activeChar.getPet() != targetSummon && !targetSummon.isDead()
                        && (targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getKarma() > 0)
                        || (targetSummon.getOwner().getInPvpZone() == true && ((L2PcInstance)activeChar).getInPvpZone()== true))
                   return new L2Character[]{targetSummon}; 
            } 
            return null; 
        }
        default:
        {
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Target type of skill is not currently handled");
            activeChar.sendPacket(sm);
            return null;
        }
    }//end switch
    }

    public final L2Object[] getTargetList(L2Character activeChar)
    {
       return getTargetList(activeChar, false); 
    }
    
    public final L2Object getFirstOfTargetList(L2Character activeChar)
    {
        L2Object[] targets;
        
        targets=getTargetList(activeChar, true);
        
        if(targets == null || targets.length == 0)
            return null;
        else
            return targets[0];
    }

    public final Func[] getStatFuncs(@SuppressWarnings("unused") L2Effect effect, L2Character player)
    {
        if (
                !(player instanceof L2PcInstance) &&
                !(player instanceof L2Attackable) &&
                !(player instanceof L2Summon)
            )
            return _emptyFunctionSet;
        if (_funcTemplates == null)
            return _emptyFunctionSet;
        List<Func> funcs = new FastList<Func>();
        for (FuncTemplate t : _funcTemplates) {
            Env env = new Env();
            env._player = player;
            env._skill = this;
            Func f = t.getFunc(env, this); // skill is owner
            if (f != null)
                funcs.add(f);
        }
        if (funcs.size() == 0)
            return _emptyFunctionSet;
        return funcs.toArray(new Func[funcs.size()]);
    }

    public boolean hasEffects()
        {
            return (_effectTemplates != null && _effectTemplates.length > 0);
        }
    
    public final L2Effect[] getEffects(L2Character effector, L2Character effected)
    {
        if (isPassive())
            return _emptyEffectSet;
        
        if (_effectTemplates == null)
            return _emptyEffectSet;
        
        if (effected instanceof L2PcInstance){
            L2PcInstance targetplayer = (L2PcInstance)effected;
            //No effect on invulnerable players unless they cast it themselves.
            if ((effector != effected) && targetplayer.isInvul()){
               return _emptyEffectSet;
            }
        }
        
        List<L2Effect> effects = new FastList<L2Effect>();
        
        for (EffectTemplate et : _effectTemplates) 
        {
            Env env = new Env();
            env._player = effector;
            env._target = effected;
            env._skill = this;
            L2Effect e = et.getEffect(env);
            if (e != null)
                effects.add(e);
        }
        
        if (effects.size() == 0)
            return _emptyEffectSet;
        
        return effects.toArray(new L2Effect[effects.size()]);
    }
    
    public final L2Effect[] getEffectsSelf(L2Character effector)
    {
        if (isPassive()) return _emptyEffectSet;
    
        if (_effectTemplatesSelf == null) return _emptyEffectSet;

        List<L2Effect> effects = new FastList<L2Effect>();

        for (EffectTemplate et : _effectTemplatesSelf)
        {
            Env env = new Env();
            env._player = effector;
            env._target = effector;
            env._skill = this;
            L2Effect e = et.getEffect(env);
            if (e != null)                
            {
                //Implements effect charge
                if (e.getEffectType()== L2Effect.EffectType.CHARGE)
                {               
                    EffectCharge effect = (EffectCharge) env._target.getEffect(L2Effect.EffectType.CHARGE);
                    if (effect != null) 
                    {
                        if (effect.num_charges < _num_charges)
                        {
                            effect.num_charges++;
                            env._target.updateEffectIcons();
                            SystemMessage sm = new SystemMessage(614);
                            sm.addString("Charged to " + effect.num_charges);
                            env._target.sendPacket(sm);
                        }                
                    }
                    else effects.add(e);
                }
                else effects.add(e);                
            }
        }
        if (effects.size() == 0) return _emptyEffectSet;

        return effects.toArray(new L2Effect[effects.size()]);
    }

    public final void attach(FuncTemplate f)
    {
        if (_funcTemplates == null)
        {
            _funcTemplates = new FuncTemplate[]{f};
        }
        else
        {
            int len = _funcTemplates.length;
            FuncTemplate[] tmp = new FuncTemplate[len+1];
            System.arraycopy(_funcTemplates, 0, tmp, 0, len);
            tmp[len] = f;
            _funcTemplates = tmp;
        }
    }

    public final void attach(EffectTemplate effect)
    {
        if (_effectTemplates == null)
        {
            _effectTemplates = new EffectTemplate[]{effect};
        }
        else
        {
            int len = _effectTemplates.length;
            EffectTemplate[] tmp = new EffectTemplate[len+1];
            System.arraycopy(_effectTemplates, 0, tmp, 0, len);
            tmp[len] = effect;
            _effectTemplates = tmp;
        }
    }
    
    public final void attachSelf(EffectTemplate effect)
    {
        if (_effectTemplatesSelf == null)
        {
            _effectTemplatesSelf = new EffectTemplate[] {effect};
        }
        else
        {
            int len = _effectTemplatesSelf.length;
            EffectTemplate[] tmp = new EffectTemplate[len + 1];
            System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
            tmp[len] = effect;
            _effectTemplatesSelf = tmp;
        }
    }

    public final void attach(Condition c, boolean itemOrWeapon)
    {
       if(itemOrWeapon) _itemPreCondition = c;
       else _preCondition = c;
    }

    public String toString()
    {
        return ""+_name+"[id="+_id+",lvl="+_level+"]";
    }
}
