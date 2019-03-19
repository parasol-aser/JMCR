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

public class ReadNode extends AbstractNode implements IMemNode
{
	private long prevSyncId,prevBranchId;
	
	private String value;
	private String addr;
	
	/**
     * Adding label file:#line to the read to identify them when analyzing the trace
     * @author Alan
     */
	private String label;
	
	public ReadNode(long GID, long tid, int ID, String addr, String value, TYPE type, String label)
	{
		super(GID, tid, ID, type, label);
		this.addr = addr;
		this.value = value;
		
		//Alan
		this.label = label;
	}

	public String getLabel(){
		return label;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public String getAddr()
	{
		return addr;
	}

	public String toString()
	{
		
			return GID+": thread "+tid+ " "+ID+" "+addr+" "+value+" "+type;
	}


	@Override
	public long getPrevSyncId() {
		// TODO Auto-generated method stub
		return prevSyncId;
	}


	@Override
	public void setPrevSyncId(long id) {
		// TODO Auto-generated method stub
		prevSyncId = id;
	}


	@Override
	public long getPrevBranchId() {
		// TODO Auto-generated method stub
		return prevBranchId;
	}


	@Override
	public void setPrevBranchId(long id) {
		// TODO Auto-generated method stub
		prevBranchId = id;
	}
	
}
