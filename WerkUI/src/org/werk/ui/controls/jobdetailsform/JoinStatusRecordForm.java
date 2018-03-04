package org.werk.ui.controls.jobdetailsform;

import java.io.IOException;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.ui.guice.LoaderFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class JoinStatusRecordForm extends GridPane {
    @FXML Label joinParameterName;
    @FXML Label waitForNJobs;
    @FXML Label joinedJobIds;
    @FXML HBox buttonBar;
    
	public JoinStatusRecordForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("JoinStatusRecordForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	public void setJoinStatusRecord(JoinStatusRecord<Long> joinStatusRecord) {
	    joinParameterName.setText("Join Parameter Name: " + joinStatusRecord.getJoinParameterName());
	    waitForNJobs.setText("Waiting For " + joinStatusRecord.getWaitForNJobs() + " Jobs: ");
	    
	    if ((joinStatusRecord.getJoinedJobIds() == null) || (joinStatusRecord.getJoinedJobIds().isEmpty()))
	    	joinedJobIds.setText("Joined Jobs: no jobs");//shouldn't happen
	    else {
	    	buttonBar.getChildren().clear();
	    	joinedJobIds.setText("Joined Jobs:");
		    for (Long jobId : joinStatusRecord.getJoinedJobIds()) {
		    	JobStatus status = joinStatusRecord.getJoinedJobStatus(jobId);
		    	Button button = new Button(jobId + "/" + status.toString());
		    	buttonBar.getChildren().add(button);
		    }
	    }
	}
}
