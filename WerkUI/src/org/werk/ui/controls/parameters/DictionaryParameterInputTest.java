package org.werk.ui.controls.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.meta.inputparameters.impl.DefaultValueJobInputParameterImpl;
import org.werk.meta.inputparameters.impl.EnumJobInputParameterImpl;
import org.werk.meta.inputparameters.impl.JobInputParameterImpl;
import org.werk.meta.inputparameters.impl.RangeJobInputParameterImpl;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.impl.BoolParameterImpl;
import org.werk.processing.parameters.impl.DictionaryParameterImpl;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.processing.parameters.impl.ListParameterImpl;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;
import org.werk.ui.controls.parameters.state.DictionaryParameterInit;

public class DictionaryParameterInputTest {
	static ListParameterImpl createListParameterImpl() {
		List<Parameter> listValues = new ArrayList<>();
		listValues.add(new LongParameterImpl(123L));
		listValues.add(new BoolParameterImpl(true));
		listValues.add(new DoubleParameterImpl(123.45));
		listValues.add(new StringParameterImpl("TEST"));
		listValues.add(createDictionaryParameterImpl(false));
		
		return new ListParameterImpl(listValues);
	}
	
	static DictionaryParameterImpl createDictionaryParameterImpl(boolean topLevel) {
		Map<String, Parameter> values = new HashMap<>();
		
		values.put("Dprm11", new LongParameterImpl(123L));
		values.put("Dprm12", new BoolParameterImpl(true));
		values.put("Dprm13", new DoubleParameterImpl(123.45));
		values.put("Dprm14", new StringParameterImpl("TEST"));
		if (topLevel) {
			values.put("Dprm15", createListParameterImpl());
			values.put("Dprm16", createDictionaryParameterImpl(false));
		}
		
		return new DictionaryParameterImpl(values);
	}
	
	static DictionaryParameterInit createJobParameterInit() {
		JobInputParameter longDefImmPrm = new DefaultValueJobInputParameterImpl("prm11", 
				ParameterType.LONG, "", true, new LongParameterImpl(123L));
		JobInputParameter boolDefImmPrm = new DefaultValueJobInputParameterImpl("prm12", 
				ParameterType.BOOL, "", true, new BoolParameterImpl(true));
		JobInputParameter doubleDefImmPrm = new DefaultValueJobInputParameterImpl("prm13", 
				ParameterType.DOUBLE, "", true, new DoubleParameterImpl(123.45));
		JobInputParameter stringDefImmPrm = new DefaultValueJobInputParameterImpl("prm14", 
				ParameterType.STRING, "", true, new StringParameterImpl("TEST"));
		JobInputParameter listDefImmPrm = new DefaultValueJobInputParameterImpl("prm15", 
				ParameterType.LIST, "", true, createListParameterImpl());
		JobInputParameter dictDefImmPrm = new DefaultValueJobInputParameterImpl("prm16", 
				ParameterType.DICTIONARY, "", true, createDictionaryParameterImpl(true));
		
		JobInputParameter longDefPrm = new DefaultValueJobInputParameterImpl("prm21", 
				ParameterType.LONG, "", false, new LongParameterImpl(1234L));
		JobInputParameter boolDefPrm = new DefaultValueJobInputParameterImpl("prm22", 
				ParameterType.BOOL, "", false, new BoolParameterImpl(false));
		JobInputParameter doubleDefPrm = new DefaultValueJobInputParameterImpl("prm23", 
				ParameterType.DOUBLE, "", false, new DoubleParameterImpl(1234.56));
		JobInputParameter stringDefPrm = new DefaultValueJobInputParameterImpl("prm24", 
				ParameterType.STRING, "", false, new StringParameterImpl("TEST2"));
		JobInputParameter listDefPrm = new DefaultValueJobInputParameterImpl("prm25", 
				ParameterType.LIST, "", false, createListParameterImpl());
		JobInputParameter dictDefPrm = new DefaultValueJobInputParameterImpl("prm26", 
				ParameterType.DICTIONARY, "", false, createDictionaryParameterImpl(true));
		
		JobInputParameter longOptPrm = new JobInputParameterImpl("prm31", ParameterType.LONG, true, "");
		JobInputParameter boolOptPrm = new JobInputParameterImpl("prm32", ParameterType.BOOL, true, "");
		JobInputParameter doubleOptPrm = new JobInputParameterImpl("prm33", ParameterType.DOUBLE, true, "");
		JobInputParameter stringOptPrm = new JobInputParameterImpl("prm34", ParameterType.STRING, true, "");
		JobInputParameter listOptPrm = new JobInputParameterImpl("prm35", ParameterType.LIST, true, "");
		JobInputParameter dictOptPrm = new JobInputParameterImpl("prm36", ParameterType.DICTIONARY, true, "");
		
		/*JobInputParameter longPrm = new JobInputParameterImpl("prm41", ParameterType.LONG, false, "");
		JobInputParameter boolPrm = new JobInputParameterImpl("prm42", ParameterType.BOOL, false, "");
		JobInputParameter doublePrm = new JobInputParameterImpl("prm43", ParameterType.DOUBLE, false, "");
		JobInputParameter stringPrm = new JobInputParameterImpl("prm44", ParameterType.STRING, false, "");
		JobInputParameter listPrm = new JobInputParameterImpl("prm45", ParameterType.LIST, false, "");
		JobInputParameter dictPrm = new JobInputParameterImpl("prm46", ParameterType.DICTIONARY, false, "");*/

		JobInputParameter longPrm = new RangeJobInputParameterImpl("prm41", ParameterType.LONG, false, "",
				new LongParameterImpl(100L), new LongParameterImpl(200L), true, false, false);
		JobInputParameter boolPrm = new JobInputParameterImpl("prm42", ParameterType.BOOL, false, "");
		JobInputParameter doublePrm = new JobInputParameterImpl("prm43", ParameterType.DOUBLE, false, "");
		List<Parameter> enumVals = new ArrayList<>();
		enumVals.add(new StringParameterImpl("ENUM_VALUE1"));
		enumVals.add(new StringParameterImpl("ENUM_VALUE2"));
		enumVals.add(new StringParameterImpl("ENUM_VALUE3"));
		JobInputParameter stringPrm = new EnumJobInputParameterImpl("prm44", ParameterType.STRING, false, "",
				enumVals, false);
		JobInputParameter listPrm = new JobInputParameterImpl("prm45", ParameterType.LIST, false, "");
		JobInputParameter dictPrm = new JobInputParameterImpl("prm46", ParameterType.DICTIONARY, false, "");
		
		List<JobInputParameter> prms = new ArrayList<>();

		prms.add(longDefImmPrm);
		prms.add(boolDefImmPrm);
		prms.add(doubleDefImmPrm);
		prms.add(stringDefImmPrm);
		prms.add(listDefImmPrm);
		prms.add(dictDefImmPrm);
		
		prms.add(longDefPrm);
		prms.add(boolDefPrm);
		prms.add(doubleDefPrm);
		prms.add(stringDefPrm);
		prms.add(listDefPrm);
		prms.add(dictDefPrm);
		
		prms.add(longOptPrm); 
		prms.add(boolOptPrm); 
		prms.add(doubleOptPrm); 
		prms.add(stringOptPrm);
		prms.add(listOptPrm);
		prms.add(dictOptPrm);
		
		prms.add(longPrm);
		prms.add(boolPrm);
		prms.add(doublePrm);
		prms.add(stringPrm);
		prms.add(listPrm);
		prms.add(dictPrm);
		
		return new DictionaryParameterInit(Optional.of(prms), true);
	}
	
	static DictionaryParameterInit createParameterInit() {
		return new DictionaryParameterInit(createDictionaryParameterImpl(true), true);
	}

	/*public DictionaryParameterInput() {
		this(createJobParameterInit(), DictionaryParameterInputType.JOB_CREATE);
		this(createParameterInit(), DictionaryParameterInputType.JOB_RESTART);
	}*/
}
