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
package com.l2jfree.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.Announcements;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SpawnTable;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.util.L2FastSet;

/**
 * @author Psycho(killer1888) / L2jfree
 */

public class HellboundManager
{
	private final static Log 	_log 						= LogFactory.getLog(InstanceManager.class);
	private int					_trustPoints 				= 0;
	private int					_currentLevel 				= 0;

	private int					_warpgateEnergy				= 0;
	private boolean				_warpgateOpen				= false;
	private int					_pointsToOpenWarpgate		= 100000;

	private final int			_level1						= 0;
	private final int			_level2						= 300000;
	private final int			_level3						= 600000;
	private final int			_level4						= 1000000;
	private final int			_level5						= 1030000;
	private final int			_level6						= 1060000;
	private final int			_level7						= 1090000;
	private final int			_level8						= 1110000;
	private final int			_level9						= 1140000;
	private final int			_level10					= 2000000;

	private FastList<L2Npc> 	_spawnRemnants				= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnKeltas				= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnDerek					= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnNatives				= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnBadNatives			= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnKiefBuron				= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnQuarryGuards			= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnQuarrySlaves			= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnHellinark				= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnOutpostCaptain		= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnWoundedLandGuards		= new FastList<L2Npc>();
	private FastList<L2Npc> 	_spawnShadai				= new FastList<L2Npc>();

	private int[][] 			_kiefBuronHarborSpawns 		= {
			{32345, -11954, 236171, -3272, 16380, 60, 60, 60},
			{32354, -20684, 250275, -3277, 12892, 60, 60, 60}};

	private int[][] 			_kiefBuronVillageSpawns 	= {
			{32345, -28504, 250132, -3480, 15879, 60, 60, 60},
			{32354, -29062, 250938, -3527, 64692, 60, 60, 60}};

	private int[][] 			_hellinarkSpawns 			= {
			{22326, -23909, 245910, -3136, 16380, 36000, 36000, 36000}};

	private int[][] 			_badNativesSpawns 			= {
			{22323, -11789, 237702, -3160, 3326, 60, 60, 60},
			{22322, -10577, 237356, -3149, 65055, 60, 60, 60},
			{22323, -10135, 237547, -3140, 3599, 60, 60, 60},
			{22323, -10661, 237586, -3131, 30769, 60, 60, 60},
			{22322, -12013, 238409, -3238, 24810, 60, 60, 60},
			{22322, -12356, 238609, -3282, 29650, 60, 60, 60},
			{22323, -12292, 238438, -3273, 52887, 60, 60, 60},
			{22323, -12636, 237769, -3230, 43213, 60, 60, 60},
			{22322, -12707, 237458, -3227, 44352, 60, 60, 60},
			{22322, -14083, 236811, -3293, 35434, 60, 60, 60},
			{22323, -14496, 236664, -3313, 41845, 60, 60, 60},
			{22322, -14639, 236636, -3318, 28059, 60, 60, 60},
			{22322, -14609, 236468, -3323, 50973, 60, 60, 60},
			{22323, -13364, 236368, -3273, 1631, 60, 60, 60},
			{22322, -12971, 236486, -3296, 3816, 60, 60, 60},
			{22323, -12820, 236699, -3294, 9951, 60, 60, 60},
			{22323, -14583, 238345, -3255, 25133, 60, 60, 60},
			{22323, -15021, 238531, -3292, 29420, 60, 60, 60},
			{22322, -16535, 238373, -3330, 32821, 60, 60, 60},
			{22323, -16959, 238296, -3345, 36123, 60, 60, 60},
			{22322, -17413, 238605, -3333, 25672, 60, 60, 60},
			{22322, -17737, 239430, -3346, 19952, 60, 60, 60},
			{22323, -18001, 239769, -3365, 19704, 60, 60, 60},
			{22323, -17932, 239864, -3366, 9831, 60, 60, 60},
			{22322, -18049, 240131, -3371, 19365, 60, 60, 60},
			{22322, -18166, 240661, -3376, 17798, 60, 60, 60},
			{22323, -18234, 241303, -3352, 15904, 60, 60, 60},
			{22322, -18361, 241412, -3344, 25370, 60, 60, 60},
			{22323, -19519, 242628, -3372, 24425, 60, 60, 60},
			{22322, -19656, 242519, -3372, 39777, 60, 60, 60},
			{22322, -20207, 242776, -3320, 28240, 60, 60, 60},
			{22322, -21160, 240982, -2857, 51672, 60, 60, 60},
			{22323, -21010, 240506, -2826, 52975, 60, 60, 60},
			{22322, -20663, 240445, -2853, 2590, 60, 60, 60},
			{22323, -20251, 240755, -2863, 7569, 60, 60, 60},
			{22323, -20092, 240668, -2863, 3751, 60, 60, 60},
			{22322, -19731, 240540, -2863, 56116, 60, 60, 60},
			{22323, -19764, 239165, -2858, 48081, 60, 60, 60},
			{22322, -19675, 238917, -2853, 52575, 60, 60, 60},
			{22322, -19956, 238359, -2782, 44076, 60, 60, 60},
			{22322, -20065, 238088, -2733, 44347, 60, 60, 60},
			{22323, -20307, 237889, -2611, 44366, 60, 60, 60},
			{22322, -20312, 239074, -2809, 20021, 60, 60, 60},
			{22323, -23048, 244030, -3121, 23552, 60, 60, 60},
			{22322, -23413, 244327, -3142, 30937, 60, 60, 60},
			{22323, -23516, 244214, -3142, 41727, 60, 60, 60},
			{22323, -24043, 245009, -3139, 26509, 60, 60, 60},
			{22322, -24456, 245273, -3142, 25605, 60, 60, 60},
			{22322, -24400, 245342, -3139, 9272, 60, 60, 60},
			{22323, -24289, 245875, -3142, 15881, 60, 60, 60},
			{22323, -23771, 246239, -3142, 2938, 60, 60, 60},
			{22322, -24184, 246829, -3140, 21354, 60, 60, 60},
			{22322, -24131, 246895, -3136, 9326, 60, 60, 60},
			{22322, -24505, 246200, -3142, 41105, 60, 60, 60},
			{22323, -24714, 245802, -3142, 44337, 60, 60, 60},
			{22323, -23633, 251135, -3317, 15517, 60, 60, 60},
			{22322, -23827, 251508, -3374, 36744, 60, 60, 60},
			{22322, -24813, 251592, -3308, 32068, 60, 60, 60},
			{22322, -25028, 252160, -3303, 17292, 60, 60, 60},
			{22322, -25199, 252162, -3278, 37604, 60, 60, 60},
			{22323, -24432, 251648, -3358, 60699, 60, 60, 60},
			{22322, -23089, 251919, -3370, 2435, 60, 60, 60},
			{22323, -22686, 251747, -3375, 61704, 60, 60, 60},
			{22323, -27193, 253489, -2189, 16530, 60, 60, 60},
			{22322, -27251, 254398, -2100, 17713, 60, 60, 60},
			{22322, -27448, 254772, -2083, 21474, 60, 60, 60},
			{22323, -27193, 255304, -2023, 10269, 60, 60, 60},
			{22322, -26802, 255580, -1961, 4395, 60, 60, 60},
			{22322, -27421, 255938, -1974, 26847, 60, 60, 60},
			{22323, -27944, 256663, -1934, 23050, 60, 60, 60},
			{22322, -28210, 257060, -1931, 21060, 60, 60, 60},
			{22323, -28041, 257288, -1939, 9346, 60, 60, 60},
			{22322, -26399, 257076, -1935, 65162, 60, 60, 60},
			{22323, -26156, 257041, -1924, 64043, 60, 60, 60},
			{22323, -25425, 257018, -2132, 280, 60, 60, 60},
			{22322, -25510, 256393, -2155, 45089, 60, 60, 60},
			{22323, -25196, 255451, -2147, 52706, 60, 60, 60},
			{22322, -25582, 254862, -2147, 48633, 60, 60, 60},
			{22323, -25732, 254391, -2151, 45744, 60, 60, 60},
			{22322, -26459, 254151, -2147, 39584, 60, 60, 60},
			{22323, -26308, 253936, -2144, 55421, 60, 60, 60},
			{22322, -28023, 253782, -2149, 31123, 60, 60, 60},
			{22323, -28535, 253708, -2149, 33574, 60, 60, 60},
			{22322, -28741, 254135, -2169, 18991, 60, 60, 60}};

	private int[][] 			_outpostCaptainSpawns 		= {
			{18466, 4919, 244032, -1932, 37604, 36000, 36000, 36000},
			{22355, 4802, 244818, -1593, 43064, 36000, 36000, 36000},
			{22355, 4663, 244987, -1590, 41082, 36000, 36000, 36000},
			{22355, 5544, 243549, -1593, 25797, 36000, 36000, 36000},
			{22355, 5558, 243294, -1588, 35167, 36000, 36000, 36000},
			{22355, 5403, 243371, -1597, 31649, 36000, 36000, 36000},
			{22356, 4803, 244281, -1930, 39050, 36000, 36000, 36000},
			{22356, 5066, 243781, -1929, 37234, 36000, 36000, 36000}};

	private int[][] 			_quarryGuardsSpawns 		= {
			{22346, -8494, 242371, -1886, 16380, 900, 900, 1100},
			{22344, -6730, 243501, -2105, 12892, 900, 900, 1100},
			{22347, -4391, 243431, -2084, 22405, 180, 180, 180},
			{22347, -5467, 243695, -2035, 32977, 180, 180, 180},
			{22347, -5253, 242252, -2072, 54337, 180, 180, 180},
			{22347, -4807, 241362, -1891, 53667, 180, 180, 180},
			{22347, -6620, 242155, -2079, 23330, 180, 180, 180},
			{22347, -7006, 243942, -2084, 18776, 180, 180, 180},
			{22347, -8158, 244221, -2089, 2104, 180, 180, 180},
			{22347, -7937, 242615, -2035, 58824, 180, 180, 180},
			{22347, -4954, 244750, -2120, 19304, 180, 180, 180},
			{22347, -5301, 245729, -2057, 14506, 180, 180, 180},
			{22347, -5352, 246972, -1892, 4050, 180, 180, 180},
			{22347, -4063, 247354, -1984, 65151, 180, 180, 180},
			{22347, -2911, 248287, -2379, 12501, 180, 180, 180},
			{22347, -3874, 248492, -2451, 55063, 180, 180, 180},
			{22347, -5001, 248211, -2465, 29686, 180, 180, 180},
			{22347, -4347, 248986, -2710, 8049, 180, 180, 180},
			{22347, -3259, 249531, -2976, 6408, 180, 180, 180},
			{22347, -5319, 247866, -2332, 48457, 180, 180, 180},				
			{22347, -3822, 246389, -1890, 44943, 180, 180, 180},										
			{22347, -5380, 246383, -2002, 33887, 180, 180, 180}};

	private int [][] 			_quarrySlaves 				= {
			{32299, -4400, 245425, -2027, 46070, 60, 60, 60},
			{32299, -4066, 244468, -2043, 60748, 60, 60, 60},
			{32299, -3714, 244117, -2044, 33672, 60, 60, 60},
			{32299, -4216, 243814, -2020, 13700, 60, 60, 60},
			{32299, -5755, 243046, -2032, 38842, 60, 60, 60},
			{32299, -5613, 242378, -2055, 46121, 60, 60, 60},
			{32299, -6812, 242386, -2043, 25159, 60, 60, 60},
			{32299, -6419, 242875, -2067, 9528, 60, 60, 60},
			{32299, -6645, 244074, -2017, 14162, 60, 60, 60},
			{32299, -6487, 244581, -2044, 45796, 60, 60, 60},
			{32299, -4807, 244946, -2070, 5925, 60, 60, 60},
			{32299, -5649, 245574, -2047, 36959, 60, 60, 60},
			{32299, -6190, 245253, -2073, 58475, 60, 60, 60},
			{32299, -6253, 245529, -2072, 61671, 60, 60, 60},
			{32299, -6290, 244398, -1975, 37604, 60, 60, 60},
			{32299, -7566, 244970, -2046, 53860, 60, 60, 60},
			{32299, -8042, 243926, -2055, 59978, 60, 60, 60},
			{32299, -7758, 243482, -2044, 12684, 60, 60, 60},
			{32299, -7254, 242713, -2031, 1358, 60, 60, 60},
			{32299, -7123, 242154, -2023, 17094, 60, 60, 60},
			{32299, -6100, 241945, -2038, 15479, 60, 60, 60},
			{32299, -5912, 241750, -2018, 14733, 60, 60, 60},
			{32299, -5100, 242540, -2064, 18278, 60, 60, 60},
			{32299, -4710, 242741, -2066, 16383, 60, 60, 60},
			{32299, -5123, 243955, -2009, 24091, 60, 60, 60},
			{32299, -5470, 244622, -2034, 39276, 60, 60, 60},
			{32299, -7389, 244290, -2050, 18480, 60, 60, 60}};

	private int[][] 			_derekSpawns 				= {
			{18465, -25675, 254686, -2144, 16380, 36000, 36000, 36000}};

	private int[][] 			_nativesSpawns 				= {
			{32362, -26979, 251120, -3520, 42440, 60, 60, 60},
			{32362, -26955, 250976, -3525, 23938, 60, 60, 60},
			{32362, -28491, 250709, -3525, 47047, 60, 60, 60},
			{32362, -28830, 250205, -3480, 8615, 60, 60, 60},
			{32362, -28773, 250709, -3525, 10472, 60, 60, 60},
			{32362, -28904, 251163, -3525, 52054, 60, 60, 60},
			{32362, -29088, 250219, -3480, 10083, 60, 60, 60},
			{32362, -27234, 251187, -3525, 39999, 60, 60, 60},
			{32362, -27874, 251724, -3525, 15577, 60, 60, 60},
			{32362, -27799, 251209, -3520, 1534, 60, 60, 60},
			{32362, -27169, 250819, -3520, 17748, 60, 60, 60},
			{32362, -27041, 251883, -3520, 35048, 60, 60, 60},
			{32362, -27405, 252043, -3520, 30046, 60, 60, 60},
			{32362, -27849, 251917, -3520, 34835, 60, 60, 60},
			{32362, -27886, 251777, -3525, 49895, 60, 60, 60},
			{32363, -29374, 253083, -3520, 43134, 60, 60, 60},
			{32363, -28531, 251087, -3525, 61999, 60, 60, 60},
			{32363, -28371, 250215, -3480, 21462, 60, 60, 60},
			{32363, -28704, 251654, -3520, 43528, 60, 60, 60},
			{32363, -28767, 252690, -3525, 17716, 60, 60, 60},
			{32363, -29646, 252915, -3520, 61217, 60, 60, 60},
			{32363, -29457, 252695, -3525, 6802, 60, 60, 60},
			{32363, -28559, 252050, -3525, 49895, 60, 60, 60},
			{32363, -25420, 252198, -3256, 0, 60, 60, 60},
			{32363, -25420, 252382, -3256, 64496, 60, 60, 60}};

	private int[][] 			_keltasAndUnderlingSpawns 	= {
			{22341, -28711, 250953, -3525, 16380, 7200, 7200, 14400},
			{22342, -24296, 252703, -3048, 7497, 60, 60, 60},
			{22342, -24207, 253277, -3040, 21487, 60, 60, 60},
			{22342, -24243, 253626, -3040, 8559, 60, 60, 60},
			{22342, -24312, 253050, -3046, 46233, 60, 60, 60},
			{22342, -24999, 252677, -3048, 29794, 60, 60, 60},
			{22342, -25054, 251866, -3288, 55437, 60, 60, 60},
			{22342, -24543, 251660, -3352, 422, 60, 60, 60},
			{22342, -24401, 251412, -3344, 46754, 60, 60, 60},
			{22342, -24063, 251225, -3328, 60265, 60, 60, 60},
			{22342, -23693, 251219, -3312, 3335, 60, 60, 60},
			{22342, -23090, 252202, -3368, 16475, 60, 60, 60},
			{22342, -23454, 252145, -3360, 34388, 60, 60, 60},
			{22342, -23690, 251817, -3368, 42646, 60, 60, 60},
			{22342, -22651, 252472, -3280, 24730, 60, 60, 60},
			{22342, -23067, 252803, -3312, 25757, 60, 60, 60},
			{22342, -22706, 252921, -3272, 13213, 60, 60, 60},
			{22342, -22456, 252950, -3274, 1204, 60, 60, 60},
			{22342, -23513, 250247, -3256, 51759, 60, 60, 60},
			{22342, -23602, 250581, -3268, 31848, 60, 60, 60},
			{22342, -24022, 252235, -3208, 27584, 60, 60, 60},
			{22342, -22133, 251747, -3352, 15634, 60, 60, 60},
			{22342, -22235, 252066, -3368, 19611, 60, 60, 60},
			{22343, -24355, 252335, -3120, 30135, 60, 60, 60},
			{22343, -23980, 252893, -3040, 17260, 60, 60, 60},
			{22343, -24424, 253550, -3046, 24542, 60, 60, 60},
			{22343, -24651, 252575, -3056, 40569, 60, 60, 60},
			{22343, -25226, 252116, -3264, 45141, 60, 60, 60},
			{22343, -25133, 251768, -3264, 42075, 60, 60, 60},
			{22343, -25012, 251641, -3268, 57091, 60, 60, 60},
			{22343, -23240, 251280, -3288, 177, 60, 60, 60},
			{22343, -23087, 251860, -3367, 13084, 60, 60, 60},
			{22343, -22237, 251493, -3352, 63309, 60, 60, 60},
			{22343, -22514, 252339, -3312, 21164, 60, 60, 60},
			{22343, -22770, 252717, -3273, 64719, 60, 60, 60},
			{22343, -23517, 251302, -3320, 43779, 60, 60, 60},
			{22343, -23658, 250880, -3264, 45788, 60, 60, 60},
			{22343, -23421, 250565, -3264, 13446, 60, 60, 60},
			{22343, -23103, 251150, -3264, 9541, 60, 60, 60},
			{22343, -24409, 252249, -3088, 32390, 60, 60, 60},
			{22343, -22256, 251467, -3352, 59532, 60, 60, 60},
			{22343, -22668, 251670, -3368, 41299, 60, 60, 60},
			{22342, -25803, 252082, -3256, 44037, 60, 60, 60},
			{22342, -25615, 252253, -3248, 13258, 60, 60, 60},
			{22342, -27308, 252489, -3525, 28246, 60, 60, 60},
			{22342, -27869, 251844, -3520, 17098, 60, 60, 60},
			{22342, -28504, 251962, -3520, 30963, 60, 60, 60},
			{22342, -27207, 250895, -3520, 23310, 60, 60, 60},
			{22342, -28903, 250759, -3520, 49978, 60, 60, 60},
			{22342, -28601, 250352, -3512, 55810, 60, 60, 60},
			{22342, -28472, 250469, -3520, 10516, 60, 60, 60},
			{22342, -28981, 250541, -3520, 32161, 60, 60, 60},
			{22342, -27860, 251184, -3520, 49313, 60, 60, 60},
			{22342, -27043, 251256, -3525, 2751, 60, 60, 60},
			{22342, -26955, 251098, -3525, 51478, 60, 60, 60},
			{22342, -25725, 251897, -3248, 4257, 60, 60, 60},
			{22342, -26940, 252385, -3525, 63610, 60, 60, 60},
			{22342, -28340, 251895, -3520, 63581, 60, 60, 60},
			{22342, -28766, 252769, -3520, 18500, 60, 60, 60},
			{22342, -29684, 253063, -3525, 21419, 60, 60, 60},
			{22342, -28848, 252819, -3520, 52985, 60, 60, 60},
			{22343, -27931, 251980, -3525, 20845, 60, 60, 60},
			{22343, -28968, 251137, -3525, 33756, 60, 60, 60},
			{22343, -28646, 250193, -3472, 46275, 60, 60, 60},
			{22343, -28687, 251753, -3525, 15584, 60, 60, 60},
			{22343, -27864, 251442, -3525, 51456, 60, 60, 60},
			{22343, -27691, 251095, -3525, 60480, 60, 60, 60},
			{22343, -26732, 251918, -3472, 3000, 60, 60, 60},
			{22343, -26958, 252092, -3520, 14817, 60, 60, 60},
			{22343, -28784, 252555, -3525, 49673, 60, 60, 60},
			{22343, -27355, 251136, -3525, 56906, 60, 60, 60},
			{22343, -28688, 251961, -3525, 49476, 60, 60, 60},
			{22343, -28538, 251183, -3525, 6437, 60, 60, 60},
			{22343, -29565, 252790, -3525, 23296, 60, 60, 60},
			{22343, -29430, 253123, -3520, 2419, 60, 60, 60},
			{22343, -25604, 252085, -3248, 5309, 60, 60, 60}};

	private int[][] 			_remnantsSpawns 			= {
			{18463, -27061, 254941, -2042, 46320, 60, 60, 60},
			{18463, -27455, 255499, -2012, 25521, 60, 60, 60},
			{18463, -27918, 256493, -1932, 27597, 60, 60, 60},
			{18463, -28073, 257065, -1933, 7677, 60, 60, 60},
			{18463, -26991, 257600, -1932, 63910, 60, 60, 60},
			{18463, -26661, 257615, -1926, 473, 60, 60, 60},
			{18463, -25730, 254439, -2153, 54360, 60, 60, 60},
			{18463, -25099, 255567, -2147, 16358, 60, 60, 60},
			{18463, -29151, 255839, -2147, 42134, 60, 60, 60},
			{18463, -29068, 254817, -2167, 50499, 60, 60, 60},
			{18463, -27982, 254332, -2153, 58681, 60, 60, 60},
			{18463, -27263, 254089, -2129, 48379, 60, 60, 60},
			{18464, -27281, 255183, -2039, 49515, 60, 60, 60},
			{18464, -27286, 255929, -1960, 12297, 60, 60, 60},
			{18464, -27583, 256373, -1924, 24848, 60, 60, 60},
			{18464, -27949, 256665, -1932, 18243, 60, 60, 60},
			{18464, -28234, 256868, -1941, 26312, 60, 60, 60},
			{18464, -27462, 257674, -1928, 4984, 60, 60, 60},
			{18464, -26343, 256435, -1933, 53950, 60, 60, 60},
			{18464, -26166, 255304, -2145, 53579, 60, 60, 60},
			{18464, -26008, 254988, -2147, 52882, 60, 60, 60},
			{18464, -25102, 254910, -2144, 19611, 60, 60, 60},
			{18464, -26666, 256235, -1911, 62890, 60, 60, 60},
			{18464, -28666, 254838, -2167, 63197, 60, 60, 60},
			{18464, -27693, 254611, -2110, 7116, 60, 60, 60},
			{18464, -27180, 253936, -2141, 54336, 60, 60, 60}};

	private int[][]				_woundedLandGuardsSpawns	= {
			{18467, -72, 234770, -3232, 26634, 60, 60, 60},
			{18467, -35, 234802, -3239, 25841, 60, 60, 60},
			{18467, -10, 234840, -3255, 26634, 60, 60, 60},
			{18467, 4, 234910, -3267, 30587, 60, 60, 60},
			{18467, 2, 234961, -3268, 31452, 60, 60, 60},
			{18467, -10, 235025, -3270, 33176, 60, 60, 60},
			{18467, -10, 235080, -3270, 32767, 60, 60, 60},
			{18467, -16, 235137, -3267, 34467, 60, 60, 60},
			{18467, -64, 235182, -3253, 35793, 60, 60, 60},
			{18467, -114, 235206, -3235, 39343, 60, 60, 60}};

	private int[][] 			_shadaiSpawns 				= {
			{32347, 8864, 253160, -1933, 51999, 60, 60, 60}};

	public static final HellboundManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void init()
	{
		_log.info("Hellbound: initialized");

		loadTrustPoints();
		loadWarpgates();

		_log.info("Hellbound: Current points: "+_trustPoints);

		_currentLevel = getHellboundLevel();

		_log.info("Hellbound: Current level: "+_currentLevel);

		revalidateWarpGates();

		_log.info("Hellbound: Warpgates energy: "+_warpgateEnergy);

		String state = (_warpgateOpen) ? "open" : "closed";
		_log.info("Hellbound: Warpgates are "+state);

		switch (_currentLevel)
		{
		case 1:
			spawnKiefBuronHarbor();
			spawnQuarrySlaves();
			spawnBadNatives();
			break;
		case 2:
			spawnKiefBuronHarbor();
			spawnQuarrySlaves();
			spawnBadNatives();
			spawnRemnants();
			break;
		case 3:
			spawnKiefBuronHarbor();
			spawnQuarrySlaves();
			spawnBadNatives();
			spawnRemnants();
			spawnKeltas();
			break;
		case 4:
			spawnKiefBuronHarbor();
			spawnQuarrySlaves();
			spawnBadNatives();
			spawnRemnants();
			spawnKeltas();
			spawnDerek();
			break;
		case 5:
			spawnKiefBuronVillage();
			spawnNatives();
			spawnQuarryGuards();
			spawnQuarrySlaves();
			break;
		case 6:
			spawnKiefBuronVillage();
			spawnNatives();
			spawnHellinark();
			break;
		case 7:
			spawnKiefBuronVillage();
			spawnNatives();
			spawnWoundedLandGuards();
			openWoundedPassage();
			break;
		case 8:
			spawnKiefBuronVillage();
			spawnNatives();
			spawnWoundedLandGuards();
			openWoundedPassage();
			spawnOutpostCaptain();
			break;
		case 9:
			spawnKiefBuronVillage();
			spawnNatives();
			spawnOutpostCaptain();
			openWoundedPassage();
			openIronGates();
			break;
		case 10:
			spawnKiefBuronVillage();
			spawnNatives();
			openWoundedPassage();
			openIronGates();
			break;
		}
		
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new saveToDB(), 15*60*1000, 15*60*1000);
	}

	public void revalidateWarpGates()
	{
		_warpgateOpen = (_warpgateEnergy >= _pointsToOpenWarpgate) ? true : false;
	}

	public int getWarpgateEnergy()
	{
		return _warpgateEnergy;
	}

	public void addWarpgateEnergy(int amount)
	{
		_warpgateEnergy += amount;

		revalidateWarpGates();
	}

	public void subWarpgateEnergy(int amount)
	{
		if (_warpgateEnergy - amount < 0)
			_warpgateEnergy = 0;
		else
			_warpgateEnergy -= amount;

		revalidateWarpGates();
	}

	public boolean isWarpgateActive()
	{
		return _warpgateOpen;
	}

	public int getHellboundLevel()
	{
		int level = 0;

		if (_trustPoints >= _level1 && _trustPoints < _level2)
			level = 1;
		else if (_trustPoints >= _level2 && _trustPoints < _level3)
			level = 2;
		else if (_trustPoints >= _level3 && _trustPoints < _level4)
			level = 3;
		else if (_trustPoints >= _level4 && _trustPoints < _level5)
			level = 4;
		else if (_trustPoints >= _level5 && _trustPoints < _level6)
			level = 5;
		else if (_trustPoints >= _level6 && _trustPoints < _level7)
			level = 6;
		else if (_trustPoints >= _level7 && _trustPoints < _level8)
			level = 7;
		else if (_trustPoints >= _level8 && _trustPoints < _level9)
			level = 8;
		else if (_trustPoints >= _level9 && _trustPoints < _level10)
			level = 9;
		else
			level = 10;

		return level;
	}

	public int getCurrentLevel()
	{
		return _currentLevel;
	}

	protected class saveToDB implements Runnable
	{
		public void run()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);

				PreparedStatement statement = con.prepareStatement("UPDATE hellbounds SET value=? WHERE variable=?");
				statement.setInt(1, _trustPoints);
				statement.setString(2, "trust_points");
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Could not save Hellbound trust points. ("+_trustPoints+" points)", e);
				return;
			}

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);

				PreparedStatement statement = con.prepareStatement("UPDATE hellbounds SET value=? WHERE variable=?");
				statement.setInt(1, _warpgateEnergy);
				statement.setString(2, "warpgates_energy");
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Could not save warpgates energy. ("+_warpgateEnergy+")", e);
				return;
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	private void loadTrustPoints()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM hellbounds WHERE variable=?");
			statement.setString(1, "trust_points");
			ResultSet rset = statement.executeQuery();
	
			if (rset.next())
				_trustPoints = rset.getInt("value");
			else
				_trustPoints = 0;
		}
		catch (Exception e)
		{
			_log.error("Cannot get Hellbound current trust points.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void loadWarpgates()
	{
		Connection con = null;
		PreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement("SELECT * FROM hellbounds WHERE variable=?");
			statement.setString(1, "warpgates_energy");
			ResultSet rset = statement.executeQuery();
	
			if (rset.next())
				_warpgateEnergy = rset.getInt("value");
			else
				_warpgateEnergy = 0;
		}
		catch (Exception e)
		{
			_log.error("Cannot get warpgates energy.", e);
		}

		double lastCheck = 0;
		try
		{
			statement.setString(1, "warpgatesLastcheck");
			ResultSet rset = statement.executeQuery();
	
			if (rset.next())
			{
				lastCheck = rset.getDouble("value");

				if (System.currentTimeMillis() >= lastCheck + (24 * 60 * 60 * 1000))
				{
					subWarpgateEnergy(10000);

					try
					{
						statement = con.prepareStatement("UPDATE hellbounds SET value=? WHERE variable=?");
						statement.setDouble(1, System.currentTimeMillis());
						statement.setString(2, "warpgatesLastcheck");
						statement.execute();
						statement.close();
					}
					catch (Exception e)
					{
						_log.error("Could not update last warpgates check.", e);
						return;
					}
				}
			}	
			else
				_warpgateEnergy = 0;
		}
		catch (Exception e)
		{
			_log.error("Cannot get warpgates last check.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private L2Spawn spawnNpc(int npcId, int x, int y, int z, int heading, int respawnTime, int respawnMinDelay, int respawnMaxDelay, boolean respawn)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);

		L2Spawn spawnDat = new L2Spawn(npcTemplate);
		spawnDat.setLocx(x);
		spawnDat.setLocy(y);
		spawnDat.setLocz(z);
		spawnDat.setAmount(1);
		spawnDat.setHeading(heading);
		spawnDat.setRespawnDelay(respawnTime);
		spawnDat.setRespawnMinDelay(respawnMinDelay);
		spawnDat.setRespawnMaxDelay(respawnMaxDelay);
		if (respawn)
			spawnDat.startRespawn();
		spawnDat.setInstanceId(0);
		SpawnTable.getInstance().addNewSpawn(spawnDat, false);

		return spawnDat;
	}

	public int getTrustPoints()
	{
		return _trustPoints;
	}

	public void addTrustPoints(int amount)
	{
		// Handling Bernarde treasure needed to up to level 4
		if (_trustPoints + amount < 999000 || _trustPoints >= _level4)
		{
			_trustPoints += amount;

			int newLevel = getHellboundLevel();
	
			if (newLevel != _currentLevel)
				handelHellboundLevelUp(newLevel);
		}
	}

	public void decreaseTrustPoints(int amount)
	{
		_trustPoints -= amount;

		int newLevel = getHellboundLevel();

		if (newLevel != _currentLevel)
			handelHellboundLevelDown(newLevel);
	}

	public void handelHellboundLevelUp(int newLevel)
	{
		switch (newLevel)
		{
		case 2:
			spawnRemnants();
			break;
		case 3:
			spawnKeltas();
			break;
		case 4:
			spawnDerek();
			break;
		case 5:
			unspawnKiefBuron();
			unspawnRemnants();
			unspawnBadNatives();
			unspawnKeltas();
			unspawnDerek();
			spawnNatives();
			spawnKiefBuronVillage();
			spawnQuarryGuards();
			spawnQuarrySlaves();
			break;
		case 6:
			unspawnQuarrySlaves();
			unspawnQuarryGuards();
			spawnHellinark();
			break;
		case 7:
			unspawnHellinark();
			spawnWoundedLandGuards();
			openWoundedPassage();
			break;
		case 8:
			unspawnWoundedLandGuards();
			spawnOutpostCaptain();
			break;
		case 9:
			openIronGates();
			break;
		}

		announceAndLog(newLevel, true);
		_currentLevel = newLevel;
	}

	public void handelHellboundLevelDown(int newLevel)
	{
		switch (newLevel)
		{
		case 1:
			unspawnRemnants();
		case 2:
			unspawnKeltas();
			spawnRemnants();
			break;
		case 3:
			unspawnDerek();
			spawnKeltas();
			break;
		case 4:
			unspawnKiefBuron();
			unspawnNatives();
			unspawnQuarryGuards();
			unspawnQuarrySlaves();
			spawnDerek();
			spawnKiefBuronHarbor();
			spawnRemnants();
			spawnBadNatives();
			spawnKeltas();
			break;
		case 5:
			unspawnHellinark();
			spawnQuarryGuards();
			spawnQuarrySlaves();
			break;
		case 6:
			unspawnWoundedLandGuards();
			closeWoundedPassage();
			spawnHellinark();
			break;
		case 7:
			unspawnOutpostCaptain();
			spawnWoundedLandGuards();
			break;
		case 8:
			unspawnShadai();
			closeIronGates();
			spawnOutpostCaptain();
			break;
		case 9:
			openIronGates();
			break;
		}

		announceAndLog(newLevel, false);
		_currentLevel = newLevel;
	}

	private void unspawnKiefBuron()
	{
		for (L2Npc npc : _spawnKiefBuron)
			npc.deleteMe();

		_spawnKiefBuron.clear();

		_log.info("Hellbound: Kief and Buron unspawned");
	}

	private void unspawnRemnants()
	{
		for (L2Npc mob: _spawnRemnants)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}

		_spawnRemnants.clear();

		_log.info("Hellbound: Remnants unspawned");
	}

	private void unspawnBadNatives()
	{
		for (L2Npc mob: _spawnBadNatives)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}

		_spawnBadNatives.clear();

		_log.info("Hellbound: Bad Natives unspawned");
	}

	private void unspawnNatives()
	{
		for (L2Npc mob: _spawnNatives)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}

		_spawnNatives.clear();

		_log.info("Hellbound: Natives unspawned");
	}

	private void unspawnKeltas()
	{
		for (L2Npc mob: _spawnKeltas)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}

		_spawnKeltas.clear();

		_log.info("Hellbound: Keltas unspawned");
	}

	private void unspawnDerek()
	{
		for (L2Npc mob: _spawnDerek)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}

		_spawnDerek.clear();

		_log.info("Hellbound: Derek unspawned");
	}

	private void unspawnQuarryGuards()
	{
		for (L2Npc mob: _spawnQuarryGuards)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}

		_spawnQuarryGuards.clear();

		_log.info("Hellbound: Quarry guards unspawned");
	}

	private void unspawnQuarrySlaves()
	{
		for (L2Npc mob: _spawnQuarrySlaves)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}

		_spawnQuarrySlaves.clear();

		_log.info("Hellbound: Quarry slaves unspawned");
	}

	private void unspawnHellinark()
	{
		for (L2Npc mob: _spawnHellinark	)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}

		_spawnHellinark	.clear();

		_log.info("Hellbound: Hellinark unspawned");
	}

	private void unspawnOutpostCaptain()
	{
		for (L2Npc mob: _spawnOutpostCaptain)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}

		_spawnOutpostCaptain.clear();

		_log.info("Hellbound: Outpost captain unspawned");
	}

	private void unspawnWoundedLandGuards()
	{
		for (L2Npc npc : _spawnWoundedLandGuards)
			npc.deleteMe();

		_spawnWoundedLandGuards.clear();

		_log.info("Hellbound: Wounded Land guards unspawned");
	}

	public void unspawnShadai()
	{
		for (L2Npc npc : _spawnShadai)
			npc.deleteMe();

		_spawnShadai.clear();

		Announcements.getInstance().announceToAll("The legendary Blacksmith Shadai has disappeared.");
		_log.info("Hellbound: Shadai unspawned");
	}

	private void spawnKiefBuronHarbor()
	{
		L2Spawn newSpawn;

		for (int[] spawnData: _kiefBuronHarborSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnKiefBuron.add(newSpawn.doSpawn());
		}

		_log.info("Hellbound: Kief and Buron Spawned in harbor");
	}

	private void spawnRemnants()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _remnantsSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnRemnants.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Remants spawned");
	}

	private void spawnKeltas()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _keltasAndUnderlingSpawns)
		{
			if (spawnData[0] == 22341)
			{
				if (getHellboundLevel() < 4)
					newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
				else
					newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], 28800, 28800, 57600, true);
			}
			else
				newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnKeltas.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Keltas spawned");
	}

	private void spawnDerek()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _derekSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnDerek.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Derek spawned");
	}

	private void spawnNatives()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _nativesSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnNatives.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Natives spawned");
	}

	private void spawnKiefBuronVillage()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _kiefBuronVillageSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnKiefBuron.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Kief and Buron spawned in Village");
	}

	private void spawnQuarryGuards()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _quarryGuardsSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnQuarryGuards.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Quarry Guards spawned");
	}

	private void spawnHellinark()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _hellinarkSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnHellinark.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Hellinark spawned");
	}

	private void spawnOutpostCaptain()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _outpostCaptainSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnOutpostCaptain.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Outpost Captain spawned");
	}

	private void spawnBadNatives()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _badNativesSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnBadNatives.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Charmed Natives spawned");
	}

	private void spawnQuarrySlaves()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _quarrySlaves)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnQuarrySlaves.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Quarry slaves spawned");
	}

	private void spawnWoundedLandGuards()
	{
		L2Spawn newSpawn;

		for (int[] spawnData : _woundedLandGuardsSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnWoundedLandGuards.add(newSpawn.doSpawn());
		}
		_log.info("Hellbound: Wounded Land guards spawned");
	}

	public void spawnShadai()
	{
		L2Spawn newSpawn;

		for (int[] spawnData: _shadaiSpawns)
		{
			newSpawn = spawnNpc(spawnData[0], spawnData[1], spawnData[2], spawnData[3], spawnData[4], spawnData[5], spawnData[6], spawnData[7], true);
			_spawnShadai.add(newSpawn.doSpawn());
		}

		Announcements.getInstance().announceToAll("The legendary Blacksmith Shadai can now be met!");
		_log.info("Hellbound: Legendary Blacksmith Shadai spawned.");
	}

	private void openWoundedPassage()
	{
		DoorTable.getInstance().getDoor(20250002).openMe();
		_log.info("Hellbound: Wounded passage open");
	}

	private void openIronGates()
	{
		DoorTable.getInstance().getDoor(20250001).openMe();
		_log.info("Hellbound: Iron Gate open");
	}

	private void closeWoundedPassage()
	{
		DoorTable.getInstance().getDoor(20250002).closeMe();
		_log.info("Hellbound: Wounded passage closed");
	}

	private void closeIronGates()
	{
		DoorTable.getInstance().getDoor(20250001).closeMe();
		_log.info("Hellbound: Iron Gate closed");
	}

	private void announceAndLog(int level, boolean up)
	{
		String announce = "Hellbound went up to stage";

		if (!up)
			announce = "Hellbound went down to stage";

		Announcements.getInstance().announceToAll(announce+" "+level);
		_log.info(announce+" "+level);
	}

	public int getNeededTrustPoints(int level)
	{
		int points = 0;

		switch (level)
		{
		case 1:
			points = _level1;
			break;
		case 2:
			points = _level2;
			break;
		case 3:
			points = _level3;
			break;
		case 4:
			points = _level4;
			break;
		case 5:
			points = _level5;
			break;
		case 6:
			points = _level6;
			break;
		case 7:
			points = _level7;
			break;
		case 8:
			points = _level8;
			break;
		case 9:
			points = _level9;
			break;
		case 10:
			points = _level10;
			break;
		default:
			points = _level10;
			break;
		}
		return points;
	}

	public void saveToDB()
	{
		new saveToDB().run();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final HellboundManager _instance = new HellboundManager();
	}
}