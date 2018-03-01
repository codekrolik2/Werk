package org.werk.engine.sql.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pillar.db.interfaces.TransactionContext;
import org.pillar.db.jdbc.JDBCTransactionContext;
import org.werk.processing.jobs.JobStatus;

public class JobLoadDAO {
	protected JobDAO jobDAO;
	
	public int deleteJoinRecords(TransactionContext tc, long awaitingJobId) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement("DELETE FROM join_record_jobs WHERE id_awaiting_job = ?");
			
			pst.setLong(1, awaitingJobId);
			
			return pst.executeUpdate();
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public int freeOwnedJobs(TransactionContext tc, long ownerId) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement("UPDATE jobs SET id_locker = ? WHERE id_locker = ?");
			
			pst.setNull(1, java.sql.Types.BIGINT);
			pst.setLong(2, ownerId);
			
			return pst.executeUpdate();
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public int acquireJobOwnership(TransactionContext tc, long ownerId, long jobId) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement("UPDATE jobs SET id_locker = ? WHERE id_job = ?");
			
			pst.setLong(1, ownerId);
			pst.setLong(2, jobId);
			
			return pst.executeUpdate();
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public int deleteUnconfirmedForkedChildJobs(TransactionContext tc, long parentJobId) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement("DELETE FROM jobs WHERE parent_job_id = ? AND status = ?");
			
			pst.setLong(1, parentJobId);
			pst.setInt(2, JobStatus.UNDEFINED.getCode());
			
			return pst.executeUpdate();
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public int activateForkedChildJobs(TransactionContext tc, Collection<Long> jobIds) throws SQLException {
		if (jobIds.isEmpty())
			return 0;
		
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE jobs SET status = ? WHERE status = ? AND id_job IN (");
			
			for (int i = 0; i < jobIds.size(); i++)
				query.append(i == 0 ? "?" : ", ?");
			
			query.append(")");
			
			pst = connection.prepareStatement(query.toString());
			
			int cnt = 1;
			pst.setInt(cnt++, JobStatus.PROCESSING.getCode());
			pst.setInt(cnt++, JobStatus.UNDEFINED.getCode());
			
			for (long jobId : jobIds)
				pst.setLong(cnt++, jobId);
			
			return pst.executeUpdate();
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public Map<Long, Long> getLoadableJobs(TransactionContext tc) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement(
					"SELECT id_job, next_execution_time " + 
					"FROM jobs " + 
					"WHERE status IN (?, ?) AND id_locker IS NULL " 
				);
			
			pst.setInt(1, JobStatus.PROCESSING.getCode());
			pst.setInt(2, JobStatus.ROLLING_BACK.getCode());
			
			ResultSet rs = pst.executeQuery();
			
			Map<Long, Long> jobIds = new HashMap<>();
			while (rs.next())
				jobIds.put(rs.getLong(1), rs.getLong(2));
			
			return jobIds;
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public Set<Long> getUnlockableJoinedJobs(TransactionContext tc) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement(
					"SELECT j1.id_job, j1.wait_for_N_jobs, COUNT(j2.id_job) as finishedCount " + 
					"FROM jobs j1, join_record_jobs jr, jobs j2 " + 
					"WHERE jr.id_awaiting_job = j1.id_job AND " + 
					"	jr.id_job = j2.id_job AND " + 
					"	j1.status = ? AND " + 
					"	j2.status IN (?, ?, ?) " + 
					"GROUP BY j1.id_job " + 
					"HAVING finishedCount >= j1.wait_for_N_jobs" 
				);
			
			pst.setInt(1, JobStatus.JOINING.getCode());
			pst.setInt(2, JobStatus.FINISHED.getCode());
			pst.setInt(3, JobStatus.ROLLED_BACK.getCode());
			pst.setInt(4, JobStatus.FAILED.getCode());
			
			ResultSet rs = pst.executeQuery();
			
			Set<Long> jobIds = new HashSet<Long>();
			while (rs.next())
				jobIds.add(rs.getLong(1));
			
			return jobIds;
		} finally {
			if (pst != null) pst.close();
		}
	}
}
