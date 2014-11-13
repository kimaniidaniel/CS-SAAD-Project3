/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
/* DECRECATED - DO NOT USE */
/**
 *
 * @author kimanii
 */
public class SimulationSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long SimulationSettingId;
    public String Name;
    public int StoragePrecision;
    public int GeographicPrecision;
    public int TemporalPrecision;
    public Date StartDate;
    public double Orbit;
    public double Tilt;
    public int GridSpacing;
    public int TimeStep;
    public int Lenght;
    public Date EntryTimeStamp;
    public HashSet<SimulationGridCell> SimulationGridCells;

    public Long getSimulationSettingId() {
        return SimulationSettingId;
    }

    public void setSimulationSettingId(Long SimulationSettingId) {
        this.SimulationSettingId = SimulationSettingId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (SimulationSettingId != null ? SimulationSettingId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the SimulationSettingId fields are not set
        if (!(object instanceof SimulationSetting)) {
            return false;
        }
        SimulationSetting other = (SimulationSetting) object;
        if ((this.SimulationSettingId == null && other.SimulationSettingId != null) || (this.SimulationSettingId != null && !this.SimulationSettingId.equals(other.SimulationSettingId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Model.SimulationSettings[ id=" + SimulationSettingId + " ]";
    }

}
