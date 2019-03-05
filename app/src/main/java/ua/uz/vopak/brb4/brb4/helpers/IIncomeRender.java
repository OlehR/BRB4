package ua.uz.vopak.brb4.brb4.helpers;

import java.util.List;

import ua.uz.vopak.brb4.brb4.models.DocWaresModel;
import ua.uz.vopak.brb4.brb4.models.DocWaresModelIncome;

// Треба розділити інтерфейс у відповідності до solid
public interface IIncomeRender {
    public void renderTable(List<DocWaresModel> model);
    public void RenderTableIncome(List<DocWaresModelIncome> model, List<DocWaresModel> inventoryModel);
}
