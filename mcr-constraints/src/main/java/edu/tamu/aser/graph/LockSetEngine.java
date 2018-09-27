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
package edu.tamu.aser.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import edu.tamu.aser.trace.LockNode;
import edu.tamu.aser.trace.LockPair;

/**
 * Engine for computing the Lockset algorithm 
 * 
 * @author jeffhuang
 *
 */
public class LockSetEngine 
{
	Vector<LockNode> locktrace;
	int n_type = 0;
	int N = 0;
	Map<Integer,Integer> lock_types = new HashMap<Integer,Integer>();
	Map<Integer,Integer> type_locks = new HashMap<Integer,Integer>();
	
	Vector<Object> lock_index = new Vector<Object>();
	
	private HashMap<String,HashMap<Long,ArrayList<LockPair>>> indexedThreadLockMaps
		= new HashMap<String,HashMap<Long,ArrayList<LockPair>>>();
	
	public void add(String addr, long tid, LockPair lp) {
		// TODO Auto-generated method stub
		HashMap<Long,ArrayList<LockPair>> threadlockmap = indexedThreadLockMaps.get(addr);
		if(threadlockmap == null)
		{
			threadlockmap = new HashMap<Long,ArrayList<LockPair>>();
			indexedThreadLockMaps.put(addr, threadlockmap);
		}
		
		ArrayList<LockPair> lockpairs = threadlockmap.get(tid);
		if(lockpairs ==null)
		{
			lockpairs = new ArrayList<LockPair>();
			threadlockmap.put(tid, lockpairs);
		}
		
		//filter out re-entrant locks for CP 
		while(!lockpairs.isEmpty())
		{
			int lastPos = lockpairs.size()-1;
			LockPair lp2 = lockpairs.get(lastPos);
			if(lp.lock==null||(lp2.lock!=null&&lp.lock.getGID()<lp2.lock.getGID()))
				lockpairs.remove(lastPos);
			else
				break;
		}
		
		
		lockpairs.add(lp);
		
		
		
	}
	//NOTE: it's possible two lockpairs overlap, because we skipped wait nodes
	public boolean hasCommonLock(long tid1, long gid1, long tid2, long gid2)
	{
		Iterator<String> keyIt 
				= indexedThreadLockMaps.keySet().iterator();
		while(keyIt.hasNext())
		{
			String key = keyIt.next();
			HashMap<Long,ArrayList<LockPair>> threadlockmap = indexedThreadLockMaps.get(key);
			ArrayList<LockPair> lockpairs1 = threadlockmap.get(tid1);
			ArrayList<LockPair> lockpairs2 = threadlockmap.get(tid2);
			if(lockpairs1!=null&&lockpairs2!=null)
			{
				boolean hasLock1 = matchAnyLockPair(lockpairs1,gid1);
				if(hasLock1)
				{
					boolean hasLock2 = matchAnyLockPair(lockpairs2,gid2);
					if(hasLock2)
						return true;
				}
			}
		}

		return false;
	}
	public boolean isAtomic(long tid1, long gid1a, long gid1b, long tid2, long gid2)
	{
		Iterator<HashMap<Long,ArrayList<LockPair>>> threadlockmapIt 
				= indexedThreadLockMaps.values().iterator();
		while(threadlockmapIt.hasNext())
		{
			HashMap<Long,ArrayList<LockPair>> threadlockmap = threadlockmapIt.next();
			ArrayList<LockPair> lockpairs1 = threadlockmap.get(tid1);
			ArrayList<LockPair> lockpairs2 = threadlockmap.get(tid2);
			if(lockpairs1!=null&&lockpairs2!=null)
			{
				boolean hasLock2 = matchAnyLockPair(lockpairs2,gid2);
				if(hasLock2)
				{
					boolean hasLock1 = matchAnyLockPair(lockpairs1,gid1a,gid1b);
					if(hasLock1)
						return true;
				}
			}
		}

		return false;
	}
	private boolean matchAnyLockPair(ArrayList<LockPair> lockpair,long gida,long gidb)
	{
		int s, e, mid;
		
		s = 0;
		e = lockpair.size()-1;
		while ( s <= e ) 
		{
			mid = ( s + e ) / 2;
			
			LockPair lp = lockpair.get(mid);
			
			if(lp.lock==null)
			{
				if(gidb<lp.unlock.getGID())
					return true;
				else
				{
					s = mid+1;
				}
			}
			else if(lp.unlock==null)
			{
				if(gida>lp.lock.getGID())
					return true;
				else
				{
					e = mid - 1;
				}
			}
			else
			{
				if(gida>lp.unlock.getGID())
					s = mid+1;
				else if(gidb<lp.lock.getGID())
					e = mid - 1;
				else if(lp.lock.getGID()<gida&&gidb<lp.unlock.getGID())
					return true;
				else
					return false;
			}
		}
		
		return false;
	}
	private boolean matchAnyLockPair(ArrayList<LockPair> lockpair,long gid)
	{
		int s, e, mid;
		
		s = 0;
		e = lockpair.size()-1;
		while ( s <= e ) 
		{
			mid = ( s + e ) / 2;
			
			LockPair lp = lockpair.get(mid);
			
			if(lp.lock==null)
			{
				if(gid<lp.unlock.getGID())
					return true;
				else
				{
					s = mid+1;
				}
			}
			else if(lp.unlock==null)
			{
				if(gid>lp.lock.getGID())
					return true;
				else
				{
					e = mid - 1;
				}
			}
			else
			{
				if(gid>lp.unlock.getGID())
					s = mid+1;
				else if(gid<lp.lock.getGID())
					e = mid - 1;
				else
					return true;
			}
		}
		
		return false;
	}





}
