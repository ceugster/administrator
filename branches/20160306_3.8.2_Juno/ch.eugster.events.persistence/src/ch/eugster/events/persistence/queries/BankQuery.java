package ch.eugster.events.persistence.queries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.zip.DataFormatException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Bank;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.service.ConnectionService;

public class BankQuery extends AbstractEntityQuery<Bank>
{
	public BankQuery(final ConnectionService service)
	{
		super(service);
	}
	
	public List<Bank> selectByBcNrAndFilialId(String bcNr, String filialId)
	{
		Expression expression = new ExpressionBuilder().get("bcNr").equal(bcNr);
		expression = expression.and(new ExpressionBuilder().get("filialId").equal(filialId));
		List<Bank> banks = this.select(Bank.class, expression);
		return banks;
	}

	public List<Bank> selectByBcNr(String bcNr)
	{
		Expression expression = new ExpressionBuilder().get("bcNr").equal(bcNr);
		List<Bank> banks = this.select(Bank.class, expression);
		return banks;
	}

	public List<Bank> selectValid()
	{
		Expression expression = new ExpressionBuilder().get("deleted").equal(false);
		List<Bank> banks = this.select(Bank.class, expression);
		return banks;
	}
	
	public long countAll()
	{
		return this.count(Bank.class, new ExpressionBuilder());
	}
	
	public int countBankListEntries(InputStream is) throws DataFormatException, IOException
	{
		int rows = 0;
		Reader r = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(r);
		try
		{
			String line = reader.readLine();
			while (line != null)
			{
				rows++;
				if (line.length() != 298)
				{
					throw new DataFormatException("Error in line " + rows + ": Invalid row length (" + line.length() + ", must be 298.");
				}
				line = reader.readLine();
			}
		}
		finally
		{
			reader.close();
		}
		return rows;
	}
	
	public void updateBankList(IProgressMonitor monitor, InputStream is, int rows) throws InterruptedException, IOException
	{
        monitor.beginTask("Banken werden aktualisiert...", rows);
		BankQuery query = (BankQuery) connectionService.getQuery(Bank.class);
		Reader r = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(r);
        try
        {
			String line = reader.readLine();
			CountryQuery countryQuery = (CountryQuery) connectionService.getQuery(Country.class);
			ZipCodeQuery zipCodeQuery = (ZipCodeQuery) connectionService.getQuery(ZipCode.class);
			int row = 0;
			while (line != null)
			{
				if (monitor.isCanceled())
				{
					throw new InterruptedException();
				}
				row++;
				List<Bank> banks = query.selectByBcNrAndFilialId(getValue(2, 5, line), getValue(7, 4, line));
				Bank bank = null;
				if (banks.isEmpty())
				{
					bank = Bank.newInstance();
				}
				else
				{
					bank = banks.get(0);
				}
				String validFrom = getValue(28, 8, line);
				if (bank.getValidFrom() == null || validFrom.compareTo(bank.getValidFrom()) > 0)
				{
					bank.setValidFrom(validFrom);
					if (!getValue(11, 5, line).isEmpty())
					{
						bank.setDeleted(true);
					}
					if (!bank.getBcNr().equals(getValue(2, 5, line)))
					{
						bank.setBcNr(getValue(2, 5, line));
					}
					if (!bank.getBcType().equals(getValue(27, 1, line)))
					{
						bank.setBcType(getValue(27, 1, line));
					}
					if (!bank.getCity().equals(getValue(194, 35, line)))
					{
						bank.setCity(getValue(194, 35, line));
					}
					if (bank.getCountry() == null || (bank.getCountry() != null && !bank.getCountry().getIso3166alpha2().equals(getValue(270, 2, line))))
					{
						String countryCode = getValue(270, 2, line);
						if (countryCode.isEmpty())
							countryCode = "CH";
						Country country = countryQuery.findByIso3166Alpha2Code(countryCode);
						bank.setCountry(country);
					}
					if (!bank.getDomicile().equals(getValue(114, 35, line)))
					{
						bank.setDomicile(getValue(114, 35, line));
					}
					if (!bank.getFax().equals(getValue(247, 18, line)))
					{
						bank.setFax(getValue(247, 18, line));
					}
					if (!bank.getFilialId().equals(getValue(7, 4, line)))
					{
						bank.setFilialId(getValue(7, 4, line));
					}
					if (!bank.getHeadOffice().equals(getValue(22, 5, line)))
					{
						bank.setHeadOffice(getValue(22, 5, line));
					}
					if (!bank.getInstitute().equals(getValue(54, 60, line)))
					{
						bank.setInstitute(getValue(54, 60, line));
					}
					if (!bank.getLanguage().equals(getValue(38, 1, line)))
					{
						bank.setLanguage(getValue(38, 1, line));
					}
					if (!bank.getPhone().equals(getValue(229, 18, line)))
					{
						bank.setPhone(getValue(229, 18, line));
					}
					if (!bank.getPostAccount().equals(getValue(272, 12, line)))
					{
						bank.setPostAccount(getValue(272, 12, line));
					}
					if (!bank.getPostAddress().equals(getValue(149, 35, line)))
					{
						bank.setPostAddress(getValue(149, 35, line));
					}
					if (!bank.getShortname().equals(getValue(39, 15, line)))
					{
						bank.setShortname(getValue(39, 15, line));
					}
					if (!bank.getSwift().equals(getValue(284, 14, line)))
					{
						bank.setSwift(getValue(284, 14, line));
					}
					if (!bank.getZip().equals(getValue(184, 10, line)))
					{
						bank.setZip(getValue(184, 10, line));
					}
					if (bank.getZipCode() == null || (bank.getZipCode() != null && !bank.getZipCode().getZip().equals(getValue(184, 10, line))))
					{
						List<ZipCode> zipCodes = zipCodeQuery.selectByZipCode(getValue(184, 10, line));
						bank.setZipCode(zipCodes.isEmpty() ? null : zipCodes.get(0));
					}
					query.merge(bank);
				}
				line = reader.readLine();
				monitor.worked(1);
			}
		} 
		finally
		{
			reader.close();
	        monitor.done();
		}
	}

	private String getValue(int start, int length, String line)
	{
		return line.substring(start, start + length).trim();
	}
	
}
