package edu.iis.mto.testreactor.doser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.iis.mto.testreactor.doser.infuser.Infuser;
import edu.iis.mto.testreactor.doser.infuser.InfuserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class MedicineDoserTest {

	@Mock
	private Clock clock;
	@Mock
	private DosageLog dosageLog;
	@Mock
	private Infuser infuser;
	private MedicineDoser medicineDoser;
	private Receipe recipe;
	private int inferiorAmount = 1;
	private final int exceedingAmount = 100;
	private final String existingMedicineName = "APAP";

	@Test
	void itCompiles() {
		assertEquals(2, 1 + 1);
	}

	@BeforeEach
	void setUp() {
		medicineDoser = new MedicineDoser(infuser, dosageLog, clock);
		recipe = Receipe.of(Medicine.of(existingMedicineName),
				Dose.of(Capacity.of(inferiorAmount, CapacityUnit.MILILITER),
						Period.of(1, TimeUnit.DAYS)), inferiorAmount);
		medicineDoser.add(MedicinePackage.of(Medicine.of(existingMedicineName), Capacity.of(100, CapacityUnit.MILILITER)));
	}

	@Test
	void dosingTest_shouldResultInSucces() {
		DosingResult dosingResult = medicineDoser.dose(recipe);
		assertEquals(DosingResult.SUCCESS, dosingResult);
	}

	@Test
	void notEnoughInMedicinesTrayTest_shouldResultInError() {
		recipe = Receipe.of(Medicine.of(existingMedicineName),
				Dose.of(Capacity.of(exceedingAmount, CapacityUnit.MILILITER),
						Period.of(1, TimeUnit.DAYS)), exceedingAmount);
		DosingResult dosingResult = medicineDoser.dose(recipe);
		assertEquals(DosingResult.ERROR, dosingResult);
	}

	@Test
	void notInMedicinesTrayTest_shouldResultInError() {
		String nonExisitngName = "IBUM";
		recipe = Receipe.of(Medicine.of(nonExisitngName),
				Dose.of(Capacity.of(inferiorAmount, CapacityUnit.MILILITER),
						Period.of(inferiorAmount, TimeUnit.DAYS)), inferiorAmount);
		DosingResult dosingResult = medicineDoser.dose(recipe);
		assertEquals(DosingResult.ERROR, dosingResult);
	}


	@Test
	void callOrderTest() throws InfuserException {
		medicineDoser.dose(recipe);
		InOrder order = Mockito.inOrder(clock, dosageLog, infuser);
		order.verify(dosageLog).logStart();
		order.verify(dosageLog).logStartDose(recipe.getMedicine(), recipe.getDose());
		order.verify(infuser).dispense( Mockito.any(), Mockito.any());
		order.verify(dosageLog).logEndDose(recipe.getMedicine(), recipe.getDose());
		order.verify(clock).wait(recipe.getDose().getPeriod());
		order.verify(dosageLog).logEnd();
	}

	@Test
	void infuserExceptionTest() throws InfuserException {
		Mockito.doThrow(InfuserException.class).when(infuser).dispense(Mockito.any(), Mockito.any());
		medicineDoser.dose(recipe);
		Mockito.verify(dosageLog).logDifuserError(recipe.getDose(), new InfuserException().getMessage());
	}




}
