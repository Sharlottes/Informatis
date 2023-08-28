package informatis.core;

import arc.Events;
import arc.func.Cons;
import arc.struct.Seq;
import mindustry.game.EventType;

/**
 * 제공받은 상태(Status)의 변화를 감지하여 제공받은 렌더러(renderer)를 재렌더링합니다.
 */
public class VDOM {
    private final Seq<BuilderData> builders = new Seq<>();

    public VDOM() {
        Events.run(EventType.Trigger.update, () -> {
            for(BuilderData builderData : builders) {
                for(Status status : builderData.statuses) {
                    if(status.prevStatus != status.currStatus) {
                        status.prevStatus = status.currStatus;
                        builderData.render.get(builderData.statuses.map(Status::getValue).toArray());
                        break;
                    }
                }
            }
        });
    }

    public VDOM addBuilder(IRebuildable buildable, Status... statuses) {
        builders.add(new BuilderData(buildable::rebuild, statuses));
        return this;
    }

    public VDOM addBuilder(Runnable render, Status... statuses) {
        addBuilder((objects) -> render.run(), statuses);
        return this;
    }

    private static class BuilderData {
        private final Seq<Status> statuses = new Seq<>();
        private final Cons<Object[]> render;

        public BuilderData(Cons<Object[]> render, Status... statuses) {
            this.render = render;
            this.statuses.addAll(statuses);
        }
    }

    public static class Status<T> {
        T prevStatus, currStatus;

        public Status() {
            this(null);
        }

        public Status(T initStatus) {
            this.prevStatus = initStatus;
            this.currStatus = initStatus;
        }

        /**
         * set currStatus to given value.<br>
         * the VDOM will detect diff prevStatus and currStatus, then call render of BuilderData.
         * @param currStatus new status value
         */
        public void setStatus(T currStatus) {
            this.currStatus = currStatus;
        }

        /**
         * get current value to be used somewhere
         * @return current status' value
         */
        public T getValue() {
            return currStatus;
        }
    }

    public interface IRebuildable {
        void rebuild(Object[] statuses);
    }
}
