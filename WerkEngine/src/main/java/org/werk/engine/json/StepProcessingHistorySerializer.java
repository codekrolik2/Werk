package org.werk.engine.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.processing.steps.StepProcessingLogRecord;

public class StepProcessingHistorySerializer {
	protected TimeProvider timeProvider;
	
	public StepProcessingHistorySerializer(TimeProvider timeProvider) {
		this.timeProvider = timeProvider;
	}
	
	public JSONArray serializeLog(List<StepProcessingLogRecord> lst) {
		JSONArray arr = new JSONArray();
		for (StepProcessingLogRecord rec : lst) {
			JSONObject obj = new JSONObject();
			obj.put("ts", rec.getTime().toString());
			obj.put("msg", rec.getMessage());
			arr.put(obj);
		}
		return arr;
	}
	
	public List<StepProcessingLogRecord> deserializeLog(JSONArray log) {
		List<StepProcessingLogRecord> lst = new ArrayList<>();
		for (int i = 0; i < log.length(); i++) {
			JSONObject obj = log.getJSONObject(i);
			String tsStr = obj.getString("ts");
			String msg = obj.getString("msg");
			
			Timestamp ts = timeProvider.createTimestamp(tsStr);
			StepProcessingLogRecord apr = new StepProcessingLogRecordImpl(ts, msg);
			lst.add(apr);
		}
		
		return lst;
	}
}
