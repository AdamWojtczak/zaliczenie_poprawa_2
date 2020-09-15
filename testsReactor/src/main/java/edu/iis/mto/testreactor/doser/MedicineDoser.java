package edu.iis.mto.testreactor.doser;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import edu.iis.mto.testreactor.doser.infuser.Infuser;
import edu.iis.mto.testreactor.doser.infuser.InfuserException;

public class MedicineDoser {

    private final Infuser infuser;
    private final Clock clock;
    private final Map<Medicine, MedicinePackage> medicinesTray = new HashMap<>();
    private final DosageLog dosageLog;

    public MedicineDoser(Infuser infuser, DosageLog dosageLog, Clock clock) {
        this.dosageLog = requireNonNull(dosageLog, "dosageLog == null");
        this.infuser = requireNonNull(infuser, "infuser == null");
        this.clock = requireNonNull(clock, "clock == null");
    }

    public void add(MedicinePackage medicine) {
        medicinesTray.put(requireNonNull(medicine, "medicine package == null").getMedicine(), medicine);
    }

    public DosingResult dose(Receipe receipe) {
        DosingResult result = DosingResult.SUCCESS;
        try {
            MedicinePackage medicinePackage = getMedicinePackage(receipe);
            dosageLog.logStart();
            for (int i = 0; i < receipe.getNumber(); i++) {
                dispenseSingleDose(receipe, medicinePackage);
                wait(receipe);
            }
        } catch (Exception e) {
            dosageLog.logError(receipe, e.getMessage());
            result = DosingResult.ERROR;
        }
        finally {
            dosageLog.logEnd();
        }
        return result;
    }

    private MedicinePackage getMedicinePackage(Receipe receipe) {
        MedicinePackage medicinePackage = medicinesTray.get(requireNonNull(receipe, "medicine == null").getMedicine());
        if (isNull(medicinePackage)) {
            throw new UnavailableMedicineException(receipe.getMedicine());
        }
        Dose dose = receipe.getDose();
        if (dose.getCapacity()
                .times(receipe.getNumber())
                .greaterThan(medicinePackage.getCapacity())) {
            throw new InsufficientMedicineException(receipe.getMedicine());
        }
        return medicinePackage;
    }

    private void dispenseSingleDose(Receipe receipe, MedicinePackage medicinePackage) {
        dosageLog.logStartDose(receipe.getMedicine(), receipe.getDose());
        try {
            infuser.dispense(medicinePackage, receipe.getDose()
                                                     .getCapacity());
            dosageLog.logEndDose(receipe.getMedicine(), receipe.getDose());
        } catch (InfuserException e) {
            dosageLog.logDifuserError(receipe.getDose(), e.getMessage());
        }

    }

    private void wait(Receipe receipe) {
        clock.wait(receipe.getDose()
                          .getPeriod());
    }

}
