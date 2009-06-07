package com.l2jfree.gameserver.network.gameserverpackets;

import java.io.IOException;
import java.util.Vector;

public class ServerStatusL2j extends GameServerBasePacket
{
	private Vector<Attribute>		_attributes;

	public static final String[]	STATUS_STRING				= { "Auto", "Good", "Normal", "Full", "Down", "Gm Only" };

	public static final int			SERVER_LIST_STATUS			= 0x01;
	public static final int			SERVER_LIST_CLOCK			= 0x02;
	public static final int			SERVER_LIST_SQUARE_BRACKET	= 0x03;
	public static final int			MAX_PLAYERS					= 0x04;
	public static final int			TEST_SERVER					= 0x05;

	public static final int			STATUS_AUTO					= 0x00;
	public static final int			STATUS_GOOD					= 0x01;
	public static final int			STATUS_NORMAL				= 0x02;
	public static final int			STATUS_FULL					= 0x03;
	public static final int			STATUS_DOWN					= 0x04;
	public static final int			STATUS_GM_ONLY				= 0x05;

	public static final int			ON							= 0x01;
	public static final int			OFF							= 0x00;

	class Attribute
	{
		public int	id;
		public int	value;

		Attribute(int pId, int pValue)
		{
			id = pId;
			value = pValue;
		}
	}

	public ServerStatusL2j()
	{
		_attributes = new Vector<Attribute>();
	}

	public void addAttribute(int id, int value)
	{
		_attributes.add(new Attribute(id, value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.l2jfree.gameserver.gameserverpackets.GameServerBasePacket#getContent
	 * ()
	 */
	@Override
	public byte[] getContent() throws IOException
	{
		writeC(0x06);
		writeD(_attributes.size());
		for (int i = 0; i < _attributes.size(); i++)
		{
			Attribute temp = _attributes.get(i);
			writeD(temp.id);
			writeD(temp.value);
		}
		return getBytes();
	}
}
