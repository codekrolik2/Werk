package org.werk.engine.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.werk.config.annotations.inject.JobParameter;
import org.werk.config.annotations.inject.StepParameter;
import org.werk.engine.processing.ContextParameterMapper;
import org.werk.engine.processing.JobContext;
import org.werk.engine.processing.StepContext;
import org.werk.processing.parameters.StringParameter;
import org.werk.processing.parameters.impl.StringParameterImpl;

public class Test {
	static class Injectable {
		@JobParameter(name="i1")
		Integer i1;
		
		@JobParameter(name="l1")
		Long l1;
		
		@JobParameter(name="d1")
		Double d1;
		
		@JobParameter(name="b1")
		Boolean b1;
		
		@JobParameter(name="s1")
		String s1;
		
		@JobParameter(name="lst1")
		List<String> lst1;
		
		@JobParameter(name="")
		Map<String, String> m1;

	
		@StepParameter(name="i2")
		Integer i2;
		
		@StepParameter(name="l2")
		Long l2;
		
		@StepParameter(name="d2")
		Double d2;
		
		@StepParameter(name="b2")
		Boolean b2;
		
		@StepParameter(name="s2")
		String s2;
		
		@StepParameter(name="lst2")
		List<String> lst2;
		
		@StepParameter(name="m2")
		Map<String, String> m2;
	}
	
	public static void main(String[] args) {
		Injectable i = new Injectable();
		
		JobContext<Long> jobContext = new JobContext<Long>(new HashMap<>());
		StepContext stepContext = new StepContext(0, new HashMap<>(), new ArrayList<>());
		
		ContextParameterMapper.remapParameters(jobContext, stepContext, i);
		
		{
			StringParameter p = (StringParameter)jobContext.getParameter("s1");
			System.out.println(p);
			System.out.println(p.getValue());
			i.s1 = "HELLO";
			System.out.println(p.getValue());
			jobContext.putParameter("s1", new StringParameterImpl("WORLD"));
			System.out.println(i.s1);
			
			//jobContext.putJobParameter("s1", new BoolParameterImpl(false));
		}
		
		{
			StringParameter p = (StringParameter)stepContext.getParameter("s2");
			System.out.println(p);
			System.out.println(p.getValue());
			i.s2 = "HELLO2";
			System.out.println(p.getValue());
			stepContext.putParameter("s2", new StringParameterImpl("WORLD2"));
			System.out.println(i.s2);
			
			//
		}
	}
}
