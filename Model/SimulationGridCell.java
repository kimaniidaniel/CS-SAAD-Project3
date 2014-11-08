/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author kimanii
 */
public class SimulationGridCell implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long GridCellId;
    private Long SimulationSettingId;
    public Double Latitude;
    public Double Longitude;
    public Double Temperature;
    public Date EntryTimeStamp;

    public Long getGridCellId() {
        return GridCellId;
    }

    public void setGridCellId(Long GridCellId) {
        this.GridCellId = GridCellId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (GridCellId != null ? GridCellId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the GridCellId fields are not set
        if (!(object instanceof SimulationGridCell)) {
            return false;
        }
        SimulationGridCell other = (SimulationGridCell) object;
        if ((this.GridCellId == null && other.GridCellId != null) || (this.GridCellId != null && !this.GridCellId.equals(other.GridCellId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Model.SimulationGridCell[ id=" + GridCellId + " ]";
    }

}
