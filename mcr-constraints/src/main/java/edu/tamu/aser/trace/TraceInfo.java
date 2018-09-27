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

import java.util.HashMap;
import java.util.HashSet;

/**
 * This class keeps the information associated with the trace such as the trace statistics,
 * shared variable signature, etc.
 * 
 * @author jeffhuang
 *
 */
public class TraceInfo {
	
	//metadata
	HashMap<Long, String> threadIdNamemap;
	HashMap<Integer, String> sharedVarIdSigMap;
	HashMap<Integer, String> stmtIdSigMap;
	HashMap<Integer, String> volatileAddresses;
	
	HashSet<String> sharedAddresses = new HashSet<String>();
	HashSet<Long> threads = new HashSet<Long>();
	int num_br,num_sync,num_rw_shared,num_rw_local,num_w_init,num_prop;

	public TraceInfo(HashMap<Integer, String> sharedVarIdSigMap2, 
			HashMap<Integer, String> volatileAddresses2, 
			HashMap<Integer, String> stmtIdSigMap2, 
			HashMap<Long, String> threadIdNameMap2)
	{
		
		sharedVarIdSigMap = sharedVarIdSigMap2;
		volatileAddresses = volatileAddresses2;
		stmtIdSigMap = stmtIdSigMap2;
		threadIdNamemap = threadIdNameMap2;
	}
	
	public void updateIdSigMap(HashMap<Integer, String> m){
		this.stmtIdSigMap = m;
	}
	public HashMap<Integer, String> getSharedVarIdMap() {
		
		return sharedVarIdSigMap;
	}
	public HashMap<Integer, String> getStmtSigIdMap() {
		
		return stmtIdSigMap;
	}
	public HashMap<Long, String> getThreadIdNameMap()
	{
		return threadIdNamemap;
	}
	public void addSharedAddresses(HashSet<String> s)
	{
		sharedAddresses.addAll(s);
	}
	public void addThreads(HashSet<Long> s)
	{
		threads.addAll(s);
	}
	public int getTraceThreadNumber() {
		// TODO Auto-generated method stub
		return threads.size();
	}
	public int getTraceSharedVariableNumber() {
		// TODO Auto-generated method stub
		return sharedAddresses.size();
	}
	public boolean isAddressVolatile(String addr) {
		
		return volatileAddresses.containsKey(Integer.valueOf(addr));
	}
	public void incrementBranchNumber()
	{
		num_br++;
	}
	public void incrementSharedReadWriteNumber()
	{
		num_rw_shared++;
	}
	public void incrementSyncNumber()
	{
		num_sync++;
	}
	public void incrementPropertyNumber()
	{
		num_prop++;
	}
	public void incrementLocalReadWriteNumber() {
		num_rw_local++;
	} 
	public void incrementInitWriteNumber() {
		num_w_init++;
	} 
	
	public int getTraceBranchNumber() {
		return num_br;
	}
	public int getTraceSharedReadWriteNumber() {
		return num_rw_shared;
	}
	public int getTraceLocalReadWriteNumber() {
		return num_rw_local;
	}
	public int getTraceInitWriteNumber() {
		return num_w_init;
	}
	public int getTraceSyncNumber() {
		return num_sync;
	}
	public int getTracePropertyNumber() {
		return num_prop;
	}
	public void setVolatileAddresses(HashSet<String> volatilevariables) {
		// TODO Auto-generated method stub
		
	}


}
