/*******************************************************************************
 * Copyright (c) 2013 University of Illinois
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package edu.tamu.aser.trace;

/**
 * An abstract representation of an event in the trace. Each event has 
 * a global id (GID) representing their order in the trace,
 * a thread id (tid) representing the identity of their thread,
 * a static syntatic ID (ID) representing their program location,
 * and a corresponding type, e.g., read, write, lock, unlock, etc.
 * For most events except branch events, they also have a corresponding 
 * address attribute "addr" denoting the memory address they access. 
 * 
 * @author smhuang
 *
 */
public abstract class AbstractNode{
	/**
	 * There are three kinds of mems: SPE, thread object id, ordinary object id
	 */
	/**
	 * 
	 */
	protected long GID;
	protected int ID;
	protected long tid;
	protected TYPE type;
	
	protected String label;
	
	public AbstractNode(long GID, long tid, int ID, TYPE type)
	{
		this.GID = GID;
		this.tid = tid;
		this.ID = ID;
		this.type = type;
		
		this.label = "other nodes";
	}
	
	public AbstractNode(long GID, long tid, int ID, TYPE type, String label)
	{
		this.GID = GID;
		this.tid = tid;
		this.ID = ID;
		this.type = type;
		
		this.label = label;
	}
	
	public long getGID()
	{
		return GID;
	}
	public int getID()
	{
		return ID;
	}
	public long getTid()
	{
		return tid;
	}

	public void setTid(int tid)
	{
		this.tid = tid;
	}
	public boolean equals(AbstractNode node)
	{
		if(this.GID == node.getGID())
		{
			return true;
		}
		else
			return false;
	}
	public TYPE getType()
	{
		return type;
	}
	
	public  String getLabel() {
		return label;
	}
	
	public enum TYPE
	{
		INIT,READ,WRITE,LOCK,UNLOCK,WAIT,NOTIFY,START,JOIN,BRANCH,BB,PROPERTY
	}
	public String toString()
	{
		return GID+": thread "+tid+" "+ID+" "+type;
	}
}
