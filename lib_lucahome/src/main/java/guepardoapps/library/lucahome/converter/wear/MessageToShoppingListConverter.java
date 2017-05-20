package guepardoapps.library.lucahome.converter.wear;

import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MessageToShoppingListConverter {

    private static final String TAG = MessageToShoppingListConverter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String SHOPPING_LIST = "ShoppingList:";

    public MessageToShoppingListConverter() {
        _logger = new LucaHomeLogger(TAG);
    }

    public SerializableList<ShoppingEntryDto> ConvertMessageToShoppingList(String message) {
        if (message.startsWith(SHOPPING_LIST)) {
            _logger.Debug("message starts with " + SHOPPING_LIST + "! replacing!");
            message = message.replace(SHOPPING_LIST, "");
        }
        _logger.Debug("message: " + message);

        String[] items = message.split("\\&");
        if (items.length > 0) {
            SerializableList<ShoppingEntryDto> list = new SerializableList<>();
            for (String entry : items) {
                String[] data = entry.split("\\:");

                _logger.Info("ShoppingList dataContent");
                for (String dataContent : data) {
                    _logger.Info(dataContent);
                }

                if (data.length == 4) {
                    if (data[0] != null && data[1] != null && data[2] != null && data[3] != null) {
                        int id = -1;

                        String name = data[0];

                        String groupString = data[1];
                        ShoppingEntryGroup group = ShoppingEntryGroup.GetByString(groupString);

                        String quantityString = data[2];
                        int quantity = -1;
                        try {
                            quantity = Integer.parseInt(quantityString);
                        } catch (Exception ex) {
                            _logger.Error(ex.toString());
                        }

                        String boughtString = data[3];
                        boolean bought = boughtString.contains("1");

                        ShoppingEntryDto item = new ShoppingEntryDto(id, name, group, quantity, bought);
                        list.addValue(item);
                    } else {
                        _logger.Warn("data[0] or data[1] or data[2] or data[3] is null!");
                    }
                } else {
                    _logger.Warn("Wrong size of shoppingListData: " + String.valueOf(data.length));
                }
            }
            return list;
        }

        _logger.Warn("Found no shopping list entry!");
        return new SerializableList<>();
    }
}
