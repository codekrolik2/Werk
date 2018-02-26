package org.werk.ui.controls.parameters.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class DictionaryParameterAndName {
	@Getter @Setter
	String name;
	@Getter @Setter
	ParameterInit init;
}
