package edu.iis.mto.testreactor.doser;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

	@Test
	void itCompiles() {
		assertEquals(2, 1 + 1);
	}

	@BeforeEach
	void setUp() {
		medicineDoser = new MedicineDoser(infuser, dosageLog, clock);
		recipe = Receipe.of(Medicine.of("APAP"),
				Dose.of(Capacity.of(1, CapacityUnit.MILILITER),
						Period.of(1, TimeUnit.DAYS)), 1);
		medicineDoser.add(MedicinePackage.of(Medicine.of("APAP"), Capacity.of(100, CapacityUnit.MILILITER)));
	}

	@Test
	void dosingTest_shouldResultInSucces() {
		DosingResult dosingResult = medicineDoser.dose(recipe);
		assertEquals(dosingResult, DosingResult.SUCCESS);
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



}
