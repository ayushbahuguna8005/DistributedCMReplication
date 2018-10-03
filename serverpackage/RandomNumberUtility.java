package serverpackage;

import java.util.ArrayList;
import java.util.Random;

public class RandomNumberUtility {
	private Random r = new Random();
	private ArrayList<Integer> randomNumberStorage = new ArrayList<Integer>();
	public int getUniqueRandomNumber() {
		int randomNumber = -1;
		while (true) {
			randomNumber = returnFiveDigitRandomNumber();
			if (!isRandomNumberIsAlreadyPresent(randomNumber)) {
				break;
			}
		}
		randomNumberStorage.add(randomNumber);
		return randomNumber;
	}

	private int returnFiveDigitRandomNumber() {
		return ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
	}

	public boolean isRandomNumberIsAlreadyPresent(int no) {
		return randomNumberStorage.contains(no);
	}
	
	public void deleteFromRandomNumberStorage(int randomNumber)
	{
		Object object = randomNumber;
		randomNumberStorage.remove(object);
	}
}
