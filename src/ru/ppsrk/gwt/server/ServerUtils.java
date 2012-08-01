package ru.ppsrk.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dozer.DozerBeanMapperSingletonWrapper;

public class ServerUtils {
	public static <ST, DT> List<DT> mapArray(Collection<ST> list,
			Class<DT> destClass) {
		List<DT> result = new ArrayList<DT>();
		for (ST elem : list) {
			if (elem != null)
				result.add(DozerBeanMapperSingletonWrapper.getInstance().map(elem,
					destClass));
		}
		return result;
	}
	
	public static <T> T mapModel(Object value, Class<T> classDTO) {
		if (value == null)
			try {
				return classDTO.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		return DozerBeanMapperSingletonWrapper.getInstance().map(value, classDTO);
	}
}
