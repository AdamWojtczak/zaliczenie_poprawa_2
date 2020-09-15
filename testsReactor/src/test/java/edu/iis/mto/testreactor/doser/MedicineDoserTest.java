package edu.iis.mto.testreactor.doser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.iis.mto.testreactor.doser.infuser.Infuser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

	@Test
	void itCompiles() {
		assertEquals(2, 1 + 1);
	}

	@BeforeEach
	void setUp() {
		medicineDoser = new MedicineDoser(infuser, dosageLog, clock);
	}

	@Test
	void dosingTest_shouldResultInSucces() {
		medicineDoser.add(MedicinePackage.of(Medicine.of("APAP"), Capacity.of(100, CapacityUnit.MILILITER)));
		Receipe recipe = Receipe.of(Medicine.of("APAP"),
                          Dose.of(Capacity.of(10, CapacityUnit.MILILITER),
                          Period.of(1, TimeUnit.DAYS)), 10);
		DosingResult dosingResult = medicineDoser.dose(recipe);
		assertEquals(dosingResult, DosingResult.SUCCESS);
	}
}
