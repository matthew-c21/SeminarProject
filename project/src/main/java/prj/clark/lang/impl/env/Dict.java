package prj.clark.lang.impl.env;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Dict extends Collection {
    private final Map<Data, Data> data;
    private final DataType type;

    public Dict(Map<Data, Data> data) {
        ensureHomogeneous(data);
        this.data = new HashMap<>(data);
        type = new DictType(
                data.keySet().stream().findFirst().map(Data::getType).orElse(Empty.get()),
                data.values().stream().findFirst().map(Data::getType).orElse(Empty.get())
        );
    }

    private static void ensureHomogeneous(Map<Data, Data> data) {
        if (data.isEmpty()) {
            return;
        }

        Map.Entry<Data, Data> first = data.entrySet().stream().findFirst().get();
        DataType key = first.getKey().getType();
        DataType value = first.getValue().getType();

        assert data.keySet().stream().map(Data::getType).allMatch(key::ofType);
        assert data.values().stream().map(Data::getType).allMatch(value::ofType);
    }

    @Override
    public Iterator<Data> iter() {
        return null;
    }

    @Override
    public DataType getType() {
        return type;
    }
}
