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
package com.l2jfree.gameserver.network.loginserverpackets;

/**
 * @author -Wooden-
 * 
 */
public class PlayerAuthResponse extends LoginServerBasePacket
{
	private final String	_account;
	private final boolean	_authed;
	private final String	_host;

	/**
	 * @param protocol
	 * @param decrypt
	 */
	public PlayerAuthResponse(int protocol, byte[] decrypt)
	{
		super(protocol, decrypt);
		_account = readS();
		_authed = (readC() != 0);
		if (!_l2j)
			_host = readS();
		else
			_host = null;
	}

	/**
	 * @return Returns the account.
	 */
	public String getAccount()
	{
		return _account;
	}

	/**
	 * @return Returns the authed state.
	 */
	public boolean isAuthed()
	{
		return _authed;
	}

	public String getHost()
	{
		return _host;
	}	
}
