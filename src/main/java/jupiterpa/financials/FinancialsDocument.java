package jupiterpa.financials;

import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.*;

import jupiterpa.util.*;

@Data
@Accessors(chain = true)
public class FinancialsDocument {
	EID documentNumber;
	TransactionType transactionType;

	List<FinancialsDocumentItem> items = new ArrayList<FinancialsDocumentItem>();
}
