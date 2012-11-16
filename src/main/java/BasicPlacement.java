/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import btrplace.model.*;
import btrplace.model.constraint.Oversubscription;
import btrplace.model.constraint.Running;
import btrplace.plan.ReconfigurationAlgorithm;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.UUID;

/**
 * Use the solver to compute a placement with regards to resources restriction.
 * @author Fabien Hermenier
 */
public class BasicPlacement {

    /**
     * Make a model of X servers having 4 CPU and 10GB of RAM
     * @return
     */
    private static Model basicEmptyModel() {
        //10 random
        IntResource pcpu = new DefaultIntResource("cpu", 4); //Each node will have 4 physical CPUs
        IntResource pmem = new DefaultIntResource("mem", 10); //Each node will have 10 GB RAM

        Model m = new DefaultModel(new DefaultMapping());
        Mapping map = m.getMapping();
        for (int i = 0; i < 10; i++) {
            UUID nId = UUID.randomUUID();
            map.addOnlineNode(nId);
        }

        m.attach(pcpu);
        m.attach(pmem);
        return m;
    }

    /**
     * Use the placement algorithm to place 30 VMs that are waiting for running.
     * There is no constraints related to the resource mapping.
     */
    public static void dumbRun() {
        Model m = basicEmptyModel();
        for (int i = 0; i < 30; i++) {
            UUID vmId = UUID.randomUUID();
            m.getMapping().addWaitingVM(vmId);
        }

        //All the VMs that are waiting must be running now
        m.attach(new Running(m.getMapping().getWaitingVMs()));
        ReconfigurationAlgorithm solver = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = solver.solve(m);
    }

    /**
     * 30 VMs to place.
     * However, we disallow memory overcommitment
     */
    public static void runWithMemoryRestriction() {
        Model m = basicEmptyModel();
        for (int i = 0; i < 30; i++) {
            UUID vmId = UUID.randomUUID();
            m.getMapping().addWaitingVM(vmId);
            m.getResource("mem").set(vmId, 2); //Each VM asks for 2GB RAM
        }

        //All the VMs that are waiting must be running now
        m.attach(new Running(m.getMapping().getWaitingVMs()));
        m.attach(new Oversubscription(m.getMapping().getOnlineNodes(), "mem", 1));
        m.attach(new Oversubscription(m.getMapping().getOnlineNodes(), "cpu", 2));
        ReconfigurationAlgorithm solver = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = solver.solve(m);
    }

    public static void main(String [] args) {

    }
}
