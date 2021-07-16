package io.mx51.spi.util;

import io.mx51.spi.model.TransactionReport;

public class TransactionReportHelper {

        public static TransactionReport createTransactionReportEnvelope(String posVendorId, String posVersion, String libraryLanguage, String libraryVersion, String serialNumber) {
            TransactionReport transactionReport = new TransactionReport();
            transactionReport.setPosVendorId(posVendorId);
            transactionReport.setPosVersion(posVersion);
            transactionReport.setLibraryLanguage(libraryLanguage);
            transactionReport.setLibraryVersion(libraryVersion);
            transactionReport.setSerialNumber(serialNumber);

            return transactionReport;
        }
}