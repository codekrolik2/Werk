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
		@JobParameter
		int i1;
		
		@JobParameter
		long l1;
		
		@JobParameter
		double d1;
		
		@JobParameter
		boolean b1;
		
		@JobParameter
		String s1;
		
		@JobParameter
		List<String> lst1;
		
		@JobParameter
		Map<String, String> m1;

	
		@StepParameter
		int i2;
		
		@StepParameter
		long l2;
		
		@StepParameter
		double d2;
		
		@StepParameter
		boolean b2;
		
		@StepParameter
		String s2;
		
		@StepParameter
		List<String> lst2;
		
		@StepParameter
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
