package ua.uz.vopak.brb4.brb4.helpers;

import java.util.List;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;

// Треба розділити інтерфейс у відповідності до solid
public interface IIncomeRender {
    public void renderTable(List<WaresItemModel> model);
}
