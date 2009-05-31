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
package com.l2jfree.gameserver.communitybbs.Manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.ShowBoard;

public class UpdateBBSManager extends BaseBBSManager
{
	private final static Log	_log	= LogFactory.getLog(UpdateBBSManager.class);

	public class UpdateItem
	{
		public Integer	id;
		public String	author;
		public String	introduction;
		public String	text;
		public String	udate;
	}

	private List<UpdateItem> getChangeLog()
	{
		List<UpdateItem> _items = new FastList<UpdateItem>();
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM changelog");
			ResultSet result = statement.executeQuery();
			while (result.next())
			{
				UpdateItem it = new UpdateItem();
				it.id = result.getInt("id");
				it.udate = result.getString("udate");
				it.introduction = result.getString("introduction");
				it.text = result.getString("text");
				it.author = result.getString("author");
				_items.add(it);
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("couldnt load changelog", e);
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return _items;
	}

	private UpdateItem getDetails(int id)
	{
		UpdateItem rit = new UpdateItem();
		for (UpdateItem temp : getChangeLog())
		{
			rit = temp;
			if (temp.id == id)
				break;
		}
		return rit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.l2jfree.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java
	 * .lang.String, com.l2jfree.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsupdate_notes"))
		{
			String content = "<html><body><br>";

			content += "<table border=0 cellspacing=0 cellpadding=2 bgcolor=808080 width=770>";
			content += "<tr>";
			content += "<td FIXWIDTH=5></td>";
			content += "<td FIXWIDTH=150 align=center>Author</td>";
			content += "<td FIXWIDTH=460 align=left>Title</td>";
			content += "<td FIXWIDTH=150 align=center>Authoring Date</td>";
			content += "<td FIXWIDTH=5></td>";
			content += "</tr></table>";
			content += "<table border=0 cellspacing=0 cellpadding=2 width=770>";
			for (UpdateItem temp : getChangeLog())
			{
				content += "<tr>";
				content += "<td FIXWIDTH=5></td><td FIXWIDTH=150>" + temp.author + "</td><td FIXWIDTH=460><a action=\"bypass _bbsupdate_details;" + temp.id + "\">" + temp.introduction + "</a></td><td FIXWIDTH=150>" + temp.udate
						+ "</td><td FIXWIDTH=5></td>";
				content += "</tr>";
			}
			content += "  </table>";
			content += "</body></html>";
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith("_bbsupdate_details"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			UpdateItem it = getDetails(id);
			String content = "<html><body><table border=0 cellspacing=0 cellpadding=2 width=770><tr><td FIXWIDTH=5></td><td><br>";
			content += it.udate + " " + it.introduction + "<br><br>";
			content += it.text;
			content += "</td><td FIXWIDTH=5></td></tr></table>  <a action=\"bypass _bbsupdate_notes\">Back to Changelog</a><br></body></html>";
			separateAndSend(content, activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.l2jfree.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String,
	 * com.l2jfree.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		// TODO Auto-generated method stub
	}

	private static UpdateBBSManager	_instance	= new UpdateBBSManager();

	/**
	 * @return
	 */
	public static UpdateBBSManager getInstance()
	{
		return _instance;
	}

}