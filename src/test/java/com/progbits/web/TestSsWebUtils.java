package com.progbits.web;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestSsWebUtils {
	
	@Test
	public void testVarious() throws Exception {
		WebFieldOptions options = new WebFieldOptions();
		
		Map<String, String[]> args = new HashMap<String, String[]>();
		
		args.put("myString", new String[] { "MyTest" } );
		args.put("myDouble", new String[] { "1,212.513" } );
		args.put("myDate", new String[] { "1973-05-14" } );
		args.put("myDecimal", new String[] { "65131.51" } );
		args.put("myInt", new String[] { "614651" } );
		args.put("myOtherInt", new String[] { "11315" } );
	
		args.put("nothing", new String[] { });
		
		MyVars vars = new MyVars();
		
		SsWebUtils.populateBean(args, vars, options);
		
		assert "MyTest".equals(vars.getMyString());
		assert new Double("1212.513").equals(vars.getMyDouble());
	}
}
