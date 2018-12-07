package jupiterpa;

import jupiterpa.util.*;

public interface IPurchasing extends IService {
	void purchase(EID materialId, int number) throws EconomyException;
}
