package app.fmt;

import entities.fmt.DatabaseLoader;
import entities.fmt.ReportGenerator;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class InvoiceReport {
	public static void main(String[] args) {
		// Print to terminal
		ReportGenerator.allSalesSummary(DatabaseLoader.invoiceMap);
		ReportGenerator.storeSalesSummary(DatabaseLoader.storeMap);
		ReportGenerator.invoiceReport(DatabaseLoader.invoiceMap);
		ReportGenerator.sortedInvoices(DatabaseLoader.invoiceMap);

		// Write same output to file
		try (PrintStream fileOut = new PrintStream(new FileOutputStream("data/output.txt"))) {
			System.setOut(fileOut);
			ReportGenerator.allSalesSummary(DatabaseLoader.invoiceMap);
			ReportGenerator.storeSalesSummary(DatabaseLoader.storeMap);
			ReportGenerator.invoiceReport(DatabaseLoader.invoiceMap);
			ReportGenerator.sortedInvoices(DatabaseLoader.invoiceMap);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}