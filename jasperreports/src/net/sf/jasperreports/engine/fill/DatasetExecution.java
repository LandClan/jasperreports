/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2019 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.engine.fill;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.repo.RepositoryContext;
import net.sf.jasperreports.repo.SimpleRepositoryContext;

/**
 * 
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 */
public class DatasetExecution
{

	private JasperReport report;
	private Map<String, Object> parameterValues;
	private JRFillDataset fillDataset;

	public DatasetExecution(RepositoryContext repositoryContext, JasperReport report, Map<String,Object> parameters)
	{
		this.report = report;
		parameterValues = parameters == null ? new HashMap<>() : new HashMap<>(parameters);
		parameterValues.put(JRParameter.JASPER_REPORT, report);
		
		ObjectFactory factory = new ObjectFactory();
		JRDataset reportDataset = report.getMainDataset();
		fillDataset = factory.getDataset(reportDataset);
		
		@SuppressWarnings("deprecation")
		JasperReportsContext depContext = 
			net.sf.jasperreports.engine.util.LocalJasperReportsContext.getLocalContext(repositoryContext.getJasperReportsContext(), parameters);
		RepositoryContext fillRepositoryContext = depContext == repositoryContext.getJasperReportsContext() ? repositoryContext
				: SimpleRepositoryContext.of(depContext, repositoryContext.getResourceContext());
		fillDataset.setRepositoryContext(fillRepositoryContext);
	}

	public void evaluateParameters(BiConsumer<JRParameter, Object> parameterConsumer) throws JRException
	{
		runWithParameters(() -> 
		{
			JRParameter[] parameters = fillDataset.getParameters();
			for (int i = 0; i < parameters.length; i++)
			{
				JRParameter param = parameters[i];
				Object value = fillDataset.getParameterValue(param.getName());
				parameterConsumer.accept(param, value);
			}
		});
	}
	
	protected void runWithParameters(Runnable action) throws JRException
	{
		fillDataset.createCalculator(report);
		fillDataset.initCalculator();

		JRResourcesFillUtil.ResourcesFillContext resourcesContext = 
			JRResourcesFillUtil.setResourcesFillContext(parameterValues);
		try
		{
			fillDataset.setParameterValues(parameterValues);
			
			action.run();
		}
		finally
		{
			fillDataset.disposeParameterContributors();
			JRResourcesFillUtil.revertResourcesFillContext(resourcesContext);
		}		
	}
	
	public void evaluateDataSource(Consumer<JRDataSource> dataSourceConsumer) throws JRException
	{
		runWithParameters(() ->
		{
			try
			{
				fillDataset.evaluateFieldProperties();
				fillDataset.initDatasource();
				
				dataSourceConsumer.accept(fillDataset.dataSource);
			}
			catch (JRException e)
			{
				throw new JRRuntimeException(e);
			}
			finally
			{
				fillDataset.closeDatasource();
			}
		});
	}

	protected static class ObjectFactory extends JRFillObjectFactory
	{
		protected ObjectFactory()
		{
			super((JRBaseFiller) null, null);
		}

		@Override
		public JRFillGroup getGroup(JRGroup group)
		{
			return super.getGroup(null);
		}
	}
	
}
